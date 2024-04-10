package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.gui.screen.RadialItems;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.utils.ControllerUtils;
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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

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
        //handlePlayerLookInput();
        handlePlayerLookInput();
        handleKeybinds();
        preventFlyDrifting();

        ControllerPlayerMovement.ensureCorrectInput(minecraft.player);
    }

    protected void handleKeybinds() {
        if (minecraft.screen != null)
            return;

        if (controller.bindings().PAUSE.justPressed()) {
            minecraft.pauseGame(false);
        }
        if (minecraft.player != null) {
            if (controller.bindings().NEXT_SLOT.justPressed()) {
                minecraft.player.getInventory().swapPaint(-1);
            }
            if (controller.bindings().PREV_SLOT.justPressed()) {
                minecraft.player.getInventory().swapPaint(1);
            }

            if (!minecraft.player.isSpectator()) {
                if (controller.bindings().DROP_STACK.justPressed()) {
                    if (minecraft.player.drop(true)) {
                        minecraft.player.swing(InteractionHand.MAIN_HAND);
                    }
                } else {
                    if (controller.bindings().DROP_INGAME.justPressed()) {
                        dropRepeating = true;
                    } else if (controller.bindings().DROP_INGAME.justReleased()) {
                        dropRepeating = false;
                    }

                    if (dropRepeating && dropRepeatHelper.shouldAction(controller.bindings().DROP_INGAME)) {
                        if (minecraft.player.drop(false)) {
                            dropRepeatHelper.onNavigate();
                            minecraft.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }

                if (controller.bindings().SWAP_HANDS.justPressed()) {
                    minecraft.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                }
            }

            if (controller.bindings().INVENTORY.justPressed()) {
                if (minecraft.gameMode.isServerControlledInventory()) {
                    minecraft.player.sendOpenInventory();
                } else {
                    minecraft.getTutorial().onOpenInventory();
                    minecraft.setScreen(new InventoryScreen(minecraft.player));
                }
            }

            if (controller.bindings().CHANGE_PERSPECTIVE.justPressed()) {
                CameraType cameraType = minecraft.options.getCameraType();
                minecraft.options.setCameraType(minecraft.options.getCameraType().cycle());
                if (cameraType.isFirstPerson() != minecraft.options.getCameraType().isFirstPerson()) {
                    minecraft.gameRenderer.checkEntityPostEffect(minecraft.options.getCameraType().isFirstPerson() ? minecraft.getCameraEntity() : null);
                }

                minecraft.levelRenderer.needsUpdate();
            }
        }
        if (controller.bindings().TOGGLE_HUD_VISIBILITY.justPressed()) {
            minecraft.options.hideGui = !minecraft.options.hideGui;
        }

        if (controller.bindings().SHOW_PLAYER_LIST.justPressed()) {
            shouldShowPlayerList = !shouldShowPlayerList;
        }

        if (controller.bindings().TOGGLE_DEBUG_MENU.justPressed()) {
            /*? if >=1.20.3 {*/
            minecraft.getDebugOverlay().toggleOverlay();
            /*?} else {*//*
            minecraft.options.renderDebug = !minecraft.options.renderDebug;
            *//*?} */
        }

        if (controller.bindings().TAKE_SCREENSHOT.justPressed()) {
            Screenshot.grab(
                    this.minecraft.gameDirectory,
                    this.minecraft.getMainRenderTarget(),
                    component -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(component))
            );
        }

        if (controller.bindings().PICK_BLOCK.justPressed()) {
            ((PickBlockAccessor) minecraft).controlify$pickBlock();
        }
        if (controller.bindings().PICK_BLOCK_NBT.justPressed()) {
            ((PickBlockAccessor) minecraft).controlify$pickBlockWithNbt();
        }

        if (controller.bindings().RADIAL_MENU.justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    controller.bindings().RADIAL_MENU,
                    RadialItems.createBindings(controller),
                    Component.translatable("controlify.radial_menu.configure_hint"),
                    null, null
            ));
        }

        if (controller.bindings().GAME_MODE_SWITCHER.justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    controller.bindings().GAME_MODE_SWITCHER,
                    RadialItems.createGameModes(),
                    Component.empty(),
                    null, null)
            );
        }

        if (controller.bindings().HOTBAR_ITEM_SELECT_RADIAL.justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(
                    controller,
                    controller.bindings().HOTBAR_ITEM_SELECT_RADIAL,
                    RadialItems.createHotbarItemSelect(),
                    Component.empty(),
                    null, null
            ));
        }

        if (this.minecraft.gameMode.hasInfiniteItems()) {
            if (controller.bindings().HOTBAR_LOAD_RADIAL.justPressed()) {
                minecraft.setScreen(new RadialMenuScreen(
                        controller,
                        controller.bindings().HOTBAR_LOAD_RADIAL,
                        RadialItems.createHotbarLoad(),
                        Component.translatable("controlify.radial.hotbar_load_hint"),
                        null, null
                ));
            }
            if (controller.bindings().HOTBAR_SAVE_RADIAL.justPressed()) {
                minecraft.setScreen(new RadialMenuScreen(
                        controller,
                        controller.bindings().HOTBAR_SAVE_RADIAL,
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

        LookInputModifier lookInputModifier = ControlifyEvents.LOOK_INPUT_MODIFIER.invoker();
        lookImpulse.x = lookInputModifier.modifyX(lookImpulse.x, controller);
        lookImpulse.y = lookInputModifier.modifyY(lookImpulse.y, controller);

        lookInputX = lookImpulse.x;
        lookInputY = lookImpulse.y;

        wasAiming = aiming;
    }

    protected void handleRegularLook(InputComponent input, Vector2f impulse, boolean aiming, LocalPlayer player) {
        InputComponent.Config config = input.confObj();

        // normal look input
        float impulseY = controller.bindings().LOOK_DOWN.state() - controller.bindings().LOOK_UP.state();
        float impulseX = controller.bindings().LOOK_RIGHT.state() - controller.bindings().LOOK_LEFT.state();

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

        if (config.requiresButton && (!controller.bindings().GAMEPAD_GYRO_BUTTON.held() && !aiming)) {
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
        float y = controller.bindings().LOOK_DOWN.state() - controller.bindings().LOOK_UP.state();
        float x = controller.bindings().LOOK_RIGHT.state() - controller.bindings().LOOK_LEFT.state();
        float flickAngle = Mth.wrapDegrees((float) Mth.atan2(y, x) * Mth.RAD_TO_DEG + 90f);

        if (!controller.bindings().LOOK_DOWN.justPressed()
                && !controller.bindings().LOOK_UP.justPressed()
                && !controller.bindings().LOOK_LEFT.justPressed()
                && !controller.bindings().LOOK_RIGHT.justPressed()
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

    public record FunctionalLookInputModifier(BiFunction<Float, ControllerEntity, Float> x, BiFunction<Float, ControllerEntity, Float> y) implements LookInputModifier {
        @Override
        public float modifyX(float x, ControllerEntity controller) {
            return this.x.apply(x, controller);
        }

        @Override
        public float modifyY(float y, ControllerEntity controller) {
            return this.y.apply(y, controller);
        }
    }
}
