package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.gui.screen.RadialItems;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.DebugOverlayHelper;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import dev.isxander.controlify.utils.animation.api.Animation;
import dev.isxander.controlify.utils.animation.api.EasingFunction;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public class InGameInputHandler {
    private final ControllerEntity controller;
    private final Controlify controlify;
    private final Minecraft minecraft;

    private double lookInputX, lookInputY; // in degrees per tick
    private final GyroState gyroInput = new GyroState();
    private boolean wasAiming;
    private Animation flickAnimation;

    private boolean shouldShowPlayerList;

    private final HoldRepeatHelper dropRepeatHelper;
    private boolean dropRepeating;

    public InGameInputHandler(ControllerEntity controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();
        this.controlify = Controlify.instance();
        this.dropRepeatHelper = new HoldRepeatHelper(20, 1);
    }

    public void inputTick() {
        handlePlayerLookInput();
        handleKeybinds();
        preventFlyDrifting();

        ControllerPlayerMovement.ensureCorrectInput(minecraft.player);
    }

    protected void handleKeybinds() {
        if (minecraft.screen != null)
            return;

        if (ControlifyBindings.PAUSE.on(controller).justPressed()) {
            minecraft.pauseGame(false);
        }
        if (minecraft.player != null) {
            if (ControlifyBindings.NEXT_SLOT.on(controller).justPressed()) {
                minecraft.player.getInventory().swapPaint(-1);
            }
            if (ControlifyBindings.PREV_SLOT.on(controller).justPressed()) {
                minecraft.player.getInventory().swapPaint(1);
            }

            if (!minecraft.player.isSpectator()) {
                if (ControlifyBindings.DROP_STACK.on(controller).justPressed()) {
                    if (minecraft.player.drop(true)) {
                        minecraft.player.swing(InteractionHand.MAIN_HAND);
                    }
                } else {
                    if (ControlifyBindings.DROP_INGAME.on(controller).justPressed()) {
                        dropRepeating = true;
                    } else if (ControlifyBindings.DROP_INGAME.on(controller).justReleased()) {
                        dropRepeating = false;
                    }

                    if (dropRepeating && dropRepeatHelper.shouldAction(ControlifyBindings.DROP_INGAME.on(controller))) {
                        if (minecraft.player.drop(false)) {
                            dropRepeatHelper.onNavigate();
                            minecraft.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }

                if (ControlifyBindings.SWAP_HANDS.on(controller).justPressed()) {
                    minecraft.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                }
            }

            if (ControlifyBindings.INVENTORY.on(controller).justPressed()) {
                if (minecraft.gameMode.isServerControlledInventory()) {
                    minecraft.player.sendOpenInventory();
                } else {
                    minecraft.getTutorial().onOpenInventory();
                    minecraft.setScreen(new InventoryScreen(minecraft.player));
                }
            }

            if (ControlifyBindings.CHANGE_PERSPECTIVE.on(controller).justPressed()) {
                CameraType cameraType = minecraft.options.getCameraType();
                minecraft.options.setCameraType(minecraft.options.getCameraType().cycle());
                if (cameraType.isFirstPerson() != minecraft.options.getCameraType().isFirstPerson()) {
                    minecraft.gameRenderer.checkEntityPostEffect(minecraft.options.getCameraType().isFirstPerson() ? minecraft.getCameraEntity() : null);
                }

                minecraft.levelRenderer.needsUpdate();
            }
        }
        if (ControlifyBindings.TOGGLE_HUD_VISIBILITY.on(controller).justPressed()) {
            minecraft.options.hideGui = !minecraft.options.hideGui;
        }

        if (ControlifyBindings.SHOW_PLAYER_LIST.on(controller).justPressed()) {
            shouldShowPlayerList = !shouldShowPlayerList;
        }


        if (ControlifyBindings.TOGGLE_DEBUG_MENU.on(controller).justPressed()) {
            DebugOverlayHelper.toggleOverlay();
        }
        if (ControlifyBindings.TOGGLE_DEBUG_MENU_FPS.on(controller).justPressed()) {
            DebugOverlayHelper.toggleFpsOverlay();
        }
        //? if >=1.20.3 {
        if (ControlifyBindings.TOGGLE_DEBUG_MENU_NET.on(controller).justPressed()) {
            DebugOverlayHelper.toggleNetworkOverlay();
        }
        if (ControlifyBindings.TOGGLE_DEBUG_MENU_PROF.on(controller).justPressed()) {
            DebugOverlayHelper.toggleProfilerOverlay();
        }
        //?} else {
        /*if (ControlifyBindings.TOGGLE_DEBUG_MENU_CHARTS.on(controller).justPressed()) {
            DebugOverlayHelper.toggleChartsOverlay();
        }
        *///?}
        if (ControlifyBindings.DEBUG_RADIAL.on(controller).justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    ControlifyBindings.DEBUG_RADIAL.on(controller),
                    RadialItems.createDebug(),
                    Component.empty(),
                    null, null
            ));
        }

        if (ControlifyBindings.TAKE_SCREENSHOT.on(controller).justPressed()) {
            Screenshot.grab(
                    this.minecraft.gameDirectory,
                    this.minecraft.getMainRenderTarget(),
                    component -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(component))
            );
        }

        if (ControlifyBindings.PICK_BLOCK.on(controller).justPressed()) {
            ((PickBlockAccessor) minecraft).controlify$pickBlock();
        }
        if (ControlifyBindings.PICK_BLOCK_NBT.on(controller).justPressed()) {
            ((PickBlockAccessor) minecraft).controlify$pickBlockWithNbt();
        }

        if (ControlifyBindings.RADIAL_MENU.on(controller).justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    ControlifyBindings.RADIAL_MENU.on(controller),
                    RadialItems.createBindings(controller),
                    Component.translatable("controlify.radial_menu.configure_hint"),
                    null, null
            ));
        }

        if (ControlifyBindings.GAME_MODE_SWITCHER.on(controller).justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    ControlifyBindings.GAME_MODE_SWITCHER.on(controller),
                    RadialItems.createGameModes(),
                    Component.empty(),
                    null, null)
            );
        }

        if (ControlifyBindings.HOTBAR_SLOT_SELECT.on(controller).justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    ControlifyBindings.HOTBAR_SLOT_SELECT.on(controller),
                    RadialItems.createHotbarItemSelect(),
                    Component.empty(),
                    null, null
            ));
        }

        if (this.minecraft.gameMode.hasInfiniteItems()) {
            if (ControlifyBindings.HOTBAR_LOAD_RADIAL.on(controller).justPressed()) {
                minecraft.setScreen(new RadialMenuScreen(
                        controller,
                        ControlifyBindings.HOTBAR_LOAD_RADIAL.on(controller),
                        RadialItems.createHotbarLoad(),
                        Component.translatable("controlify.radial.hotbar_load_hint"),
                        null, null
                ));
            }
            if (ControlifyBindings.HOTBAR_SAVE_RADIAL.on(controller).justPressed()) {
                minecraft.setScreen(new RadialMenuScreen(
                        controller,
                        ControlifyBindings.HOTBAR_SAVE_RADIAL.on(controller),
                        RadialItems.createHotbarSave(),
                        Component.translatable("controlify.radial.hotbar_save_hint"),
                        null, null
                ));
            }
        }
    }

    protected void handlePlayerLookInput() {
        LocalPlayer player = this.minecraft.player;

        boolean mouseNotGrabbed = !minecraft.mouseHandler.isMouseGrabbed();
        boolean outOfFocus = !minecraft.isWindowActive() && !controlify.config().globalSettings().outOfFocusInput;
        boolean screenVisible = minecraft.screen != null;
        boolean playerExists = minecraft.player != null;
        if (mouseNotGrabbed || outOfFocus || screenVisible || !playerExists) {
            lookInputX = 0;
            lookInputY = 0;
            return;
        }

        boolean aiming = isAiming(player);

        Vector2f lookImpulse = new Vector2f();
        controller.gyro().ifPresent(gyro -> handleGyroLook(gyro, lookImpulse, aiming));

        if (controller.gyro().map(gyro -> gyro.confObj().lookSensitivity > 0 && gyro.confObj().flickStick).orElse(false)) {
            handleFlickStick(player);
        } else {
            controller.input().ifPresent(input -> handleRegularLook(input, lookImpulse, aiming, player));
        }

        var modifier = new LookInputModifier(new Vector2f(lookImpulse), controller);
        ControlifyEvents.LOOK_INPUT_MODIFIER.invoke(modifier);
        lookImpulse.set(modifier.lookInput());

        lookInputX = lookImpulse.x;
        lookInputY = lookImpulse.y;

        wasAiming = aiming;
    }

    protected void handleRegularLook(InputComponent input, Vector2f impulse, boolean aiming, LocalPlayer player) {
        InputComponent.Config config = input.confObj();

        // normal look input
        float impulseY = ControlifyBindings.LOOK_DOWN.on(controller).analogueNow()
                - ControlifyBindings.LOOK_UP.on(controller).analogueNow();
        float impulseX = ControlifyBindings.LOOK_RIGHT.on(controller).analogueNow()
                - ControlifyBindings.LOOK_LEFT.on(controller).analogueNow();

        // apply the easing on its length to preserve circularity
        Vector2fc easedImpulse = ControllerUtils.applyEasingToLength(
                impulseX,
                impulseY,
                x -> x * Math.abs(x)
        );
        impulseX = easedImpulse.x();
        impulseY = easedImpulse.y();

        impulseX *= config.hLookSensitivity * 10f; // 10 degrees per second at 100% sensitivity
        impulseY *= config.vLookSensitivity * 10f;

        if (config.reduceAimingSensitivity && player.isUsingItem()) {
            float aimMultiplier = switch (player.getUseItem().getUseAnimation()) {
                case BOW, SPEAR -> 0.6f;
                case SPYGLASS -> 0.2f;
                default -> 1f;
            };
            impulseX *= aimMultiplier;
            impulseY *= aimMultiplier;
        }

        impulse.x += impulseX;
        impulse.y += impulseY;
    }

    protected void handleGyroLook(GyroComponent gyro, Vector2f impulse, boolean aiming) {
        GyroComponent.Config config = gyro.confObj();

        if (config.requiresButton && (!ControlifyBindings.GYRO_BUTTON.on(controller).digitalNow() && !aiming)) {
            gyroInput.set(0);
        } else {
            if (config.relativeGyroMode)
                gyroInput.add(new GyroState(gyro.getState()).mul(0.1f));
            else
                gyroInput.set(gyro.getState());
        }

        // convert radians per second into degrees per tick
        GyroState thisInput = new GyroState(gyroInput)
                .mul(Mth.RAD_TO_DEG)
                .div(20)
                .mul(config.lookSensitivity);

        impulse.y += -thisInput.pitch() * (config.invertY ? -1 : 1);
        impulse.x += switch (config.yawMode) {
            case YAW -> -thisInput.yaw();
            case ROLL -> -thisInput.roll();
            case BOTH -> -thisInput.yaw() - thisInput.roll();
        } * (config.invertX ? -1 : 1);
    }

    protected void handleFlickStick(LocalPlayer player) {
        float y = ControlifyBindings.LOOK_DOWN.on(controller).analogueNow()
                - ControlifyBindings.LOOK_UP.on(controller).analogueNow();
        float x = ControlifyBindings.LOOK_RIGHT.on(controller).analogueNow()
                - ControlifyBindings.LOOK_LEFT.on(controller).analogueNow();

        float flickAngle = Mth.wrapDegrees((float) Mth.atan2(y, x) * Mth.RAD_TO_DEG + 90f);

        if (!ControlifyBindings.LOOK_DOWN.on(controller).justPressed()
                && !ControlifyBindings.LOOK_UP.on(controller).justPressed()
                && !ControlifyBindings.LOOK_LEFT.on(controller).justPressed()
                && !ControlifyBindings.LOOK_RIGHT.on(controller).justPressed()
        ) {
            return;
        }

        if (flickAnimation != null && flickAnimation.isPlaying()) {
            flickAnimation.skipToEnd();
        }

        flickAnimation = Animation.of(8)
                .easing(EasingFunction.EASE_OUT_EXPO)
                .deltaConsumerD(angle -> player.turn(angle, 0), 0, flickAngle / 0.15)
                .play();
    }

    public void processPlayerLook(float deltaTime) {
        if (minecraft.player != null) {
            minecraft.player.turn(lookInputX / 0.15f * deltaTime, lookInputY / 0.15f * deltaTime);
        }
    }

    public boolean shouldShowPlayerList() {
        return this.shouldShowPlayerList;
    }

    public void preventFlyDrifting() {
        if (!controller.genericConfig().config().disableFlyDrifting || !ServerPolicies.DISABLE_FLY_DRIFTING.get().isAllowed()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player != null && player.getAbilities().flying && !player.onGround()) {
            Vec3 motion = player.getDeltaMovement();
            double x = motion.x;
            double y = motion.y;
            double z = motion.z;

            if (!player.input.jumping)
                y = Math.min(y, 0);
            if (!player.input.shiftKeyDown)
                y = Math.max(y, 0);

            if (player.input.forwardImpulse == 0 && player.input.leftImpulse == 0) {
                x = 0;
                z = 0;
            }

            player.setDeltaMovement(x, y, z);
        }
    }

    private boolean isAiming(Player player) {
        return player.isUsingItem() && switch (player.getUseItem().getUseAnimation()) {
            case BOW, CROSSBOW, SPEAR, SPYGLASS -> true;
            default -> false;
        };
    }
}
