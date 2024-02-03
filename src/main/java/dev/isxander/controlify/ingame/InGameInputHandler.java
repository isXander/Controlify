package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.composable.gyro.GyroState;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.utils.Animator;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.Easings;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2fc;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class InGameInputHandler {
    private final Controller<?> controller;
    private final Minecraft minecraft;

    private double lookInputX, lookInputY; // in degrees per tick
    private final GyroState gyroInput = new GyroState();
    private boolean wasAiming;

    private boolean shouldShowPlayerList;

    private final HoldRepeatHelper dropRepeatHelper;
    private boolean dropRepeating;

    public InGameInputHandler(Controller<?> controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();
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
            minecraft.getDebugOverlay().toggleOverlay();
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
            minecraft.setScreen(new RadialMenuScreen(controller, false, null));
        }
    }

    protected void handlePlayerLookInput() {
        var player = this.minecraft.player;

        if (!minecraft.mouseHandler.isMouseGrabbed() || (!minecraft.isWindowActive() && !Controlify.instance().config().globalSettings().outOfFocusInput) || minecraft.screen != null || player == null) {
            lookInputX = 0;
            lookInputY = 0;
            return;
        }

        boolean isAiming = isAiming(player);

        float impulseY = 0f;
        float impulseX = 0f;

        // flick stick - turn 90 degrees immediately upon turning
        // should be paired with gyro controls
        if (controller.config().flickStick) {
            var turnAngle = 90 / 0.15f; // Entity#turn multiplies cursor delta by 0.15 to get rotation

            float flick = controller.bindings().LOOK_DOWN.justPressed() || controller.bindings().LOOK_RIGHT.justPressed() ? 1 : controller.bindings().LOOK_UP.justPressed() || controller.bindings().LOOK_LEFT.justPressed() ? -1 : 0;

            if (flick != 0f) {
                AtomicReference<Float> lastAngle = new AtomicReference<>(0f);
                Animator.INSTANCE.play(new Animator.AnimationInstance(10, Easings::easeOutExpo)
                        .addConsumer(angle -> {
                            player.turn((angle - lastAngle.get()) * flick, 0);
                            lastAngle.set(angle);
                        }, 0, turnAngle));
            }
        } else {
            // TODO: refactor this function majorly - this is truly awful
            //       possibly separate the flick stick code into its own function?
            // normal look input
            impulseY = controller.bindings().LOOK_DOWN.state() - controller.bindings().LOOK_UP.state();
            impulseX = controller.bindings().LOOK_RIGHT.state() - controller.bindings().LOOK_LEFT.state();

            // apply the easing on its length to preserve circularity
            Vector2fc easedImpulse = ControllerUtils.applyEasingToLength(
                    impulseX,
                    impulseY,
                    x -> x * Math.abs(x)
            );
            impulseX = easedImpulse.x();
            impulseY = easedImpulse.y();

            impulseX *= controller.config().horizontalLookSensitivity * 10f; // 10 degrees per second at 100% sensitivity
            impulseY *= controller.config().verticalLookSensitivity * 10f;

            if (controller.config().reduceAimingSensitivity && player.isUsingItem()) {
                float aimMultiplier = switch (player.getUseItem().getUseAnimation()) {
                    case BOW, SPEAR -> 0.6f;
                    case SPYGLASS -> 0.2f;
                    default -> 1f;
                };
                impulseX *= aimMultiplier;
                impulseY *= aimMultiplier;
            }
        }

        // gyro input
        if (controller.supportsGyro()) {
            boolean useGyro = false;

            if (controller.config().gyroRequiresButton) {
                if (controller.bindings().GAMEPAD_GYRO_BUTTON.justPressed() || (isAiming && !wasAiming))
                    gyroInput.set(0);

                if (controller.bindings().GAMEPAD_GYRO_BUTTON.held() || isAiming) {
                    if (controller.config().relativeGyroMode)
                        gyroInput.add(new GyroState(controller.state().getGyroState()).mul(0.1f));
                    else
                        gyroInput.set(controller.state().getGyroState());
                    useGyro = true;
                }
            } else {
                gyroInput.set(controller.state().getGyroState());
                useGyro = true;
            }

            if (useGyro) {
                // convert radians per second into degrees per tick
                GyroState thisInput = new GyroState(gyroInput)
                        .mul(Mth.RAD_TO_DEG)
                        .div(20)
                        .mul(controller.config().gyroLookSensitivity);

                impulseY += -thisInput.pitch() * (controller.config().invertGyroY ? -1 : 1);
                impulseX += switch (controller.config().gyroYawMode) {
                    case YAW -> -thisInput.yaw();
                    case ROLL -> -thisInput.roll();
                    case BOTH -> -thisInput.yaw() - thisInput.roll();
                } * (controller.config().invertGyroX ? -1 : 1);
            }
        }

        LookInputModifier lookInputModifier = ControlifyEvents.LOOK_INPUT_MODIFIER.invoker();
        impulseX = lookInputModifier.modifyX(impulseX, controller);
        impulseY = lookInputModifier.modifyY(impulseY, controller);

        lookInputX = impulseX;
        lookInputY = impulseY;

        wasAiming = isAiming;
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
        if (!controller.config().disableFlyDrifting || !ServerPolicies.DISABLE_FLY_DRIFTING.get().isAllowed()) {
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

    public record FunctionalLookInputModifier(BiFunction<Float, Controller<?>, Float> x, BiFunction<Float, Controller<?>, Float> y) implements LookInputModifier {
        @Override
        public float modifyX(float x, Controller<?> controller) {
            return this.x.apply(x, controller);
        }

        @Override
        public float modifyY(float y, Controller<?> controller) {
            return this.y.apply(y, controller);
        }
    }
}
