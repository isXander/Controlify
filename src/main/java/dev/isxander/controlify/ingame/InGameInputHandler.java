package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.utils.Animator;
import dev.isxander.controlify.utils.Easings;
import dev.isxander.controlify.utils.NavigationHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class InGameInputHandler {
    private final Controller<?, ?> controller;
    private final Minecraft minecraft;

    private double lookInputX, lookInputY; // in degrees per tick
    private final GamepadState.GyroState gyroInput = new GamepadState.GyroState();
    private boolean wasAiming;

    private boolean shouldShowPlayerList;

    private final NavigationHelper dropRepeatHelper;

    public InGameInputHandler(Controller<?, ?> controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();
        this.dropRepeatHelper = new NavigationHelper(20, 1);
    }

    public void inputTick() {
        handlePlayerLookInput();
        handleKeybinds();
    }

    protected void handleKeybinds() {
        shouldShowPlayerList = false;

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
                } else if (dropRepeatHelper.shouldAction(controller.bindings().DROP)) {
                    if (minecraft.player.drop(false)) {
                        dropRepeatHelper.onNavigate();
                        minecraft.player.swing(InteractionHand.MAIN_HAND);
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

        shouldShowPlayerList = controller.bindings().SHOW_PLAYER_LIST.held();

        if (controller.bindings().RADIAL_MENU.justPressed()) {
            minecraft.setScreen(new RadialMenuScreen(controller));
        }
    }

    protected void handlePlayerLookInput() {
        var player = this.minecraft.player;
        var gamepad = controller instanceof GamepadController ? (GamepadController) controller : null;

        if (!minecraft.mouseHandler.isMouseGrabbed() || (!minecraft.isWindowActive() && !Controlify.instance().config().globalSettings().outOfFocusInput) || minecraft.screen != null || player == null) {
            lookInputX = 0;
            lookInputY = 0;
            return;
        }

        var isAiming = isAiming(player);

        float impulseY = 0f;
        float impulseX = 0f;

        // flick stick - turn 90 degrees immediately upon turning
        // should be paired with gyro controls
        if (gamepad != null && gamepad.config().flickStick) {
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
            // normal look input
            impulseY = controller.bindings().LOOK_DOWN.state() - controller.bindings().LOOK_UP.state();
            impulseX = controller.bindings().LOOK_RIGHT.state() - controller.bindings().LOOK_LEFT.state();
            impulseX *= Math.abs(impulseX) * 10f; // 10 degrees per second
            impulseY *= Math.abs(impulseY) * 10f;
            impulseX *= controller.config().horizontalLookSensitivity;
            impulseY *= controller.config().verticalLookSensitivity;

            if (controller.config().reduceAimingSensitivity && player.isUsingItem()) {
                float aimMultiplier = switch (player.getUseItem().getUseAnimation()) {
                    case BOW, CROSSBOW, SPEAR -> 0.6f;
                    case SPYGLASS -> 0.2f;
                    default -> 1f;
                };
                impulseX *= aimMultiplier;
                impulseY *= aimMultiplier;
            }
        }

        // gyro input
        if (gamepad != null && gamepad.hasGyro()) {
            boolean useGyro = false;

            if (gamepad.config().gyroRequiresButton) {
                if (gamepad.bindings().GAMEPAD_GYRO_BUTTON.justPressed() || (isAiming && !wasAiming))
                    gyroInput.set(0);

                if (gamepad.bindings().GAMEPAD_GYRO_BUTTON.held() || isAiming) {
                    if (gamepad.config().relativeGyroMode)
                        gyroInput.add(new Vector3f(gamepad.state().gyroDelta()).mul(0.1f));
                    else
                        gyroInput.set(gamepad.state().gyroDelta());
                    useGyro = true;
                }
            } else {
                gyroInput.set(gamepad.state().gyroDelta());
                useGyro = true;
            }

            if (useGyro) {
                // convert radians per second into degrees per tick
                GamepadState.GyroState thisInput = new GamepadState.GyroState(gyroInput)
                        .mul(Mth.RAD_TO_DEG)
                        .div(20)
                        .mul(gamepad.config().gyroLookSensitivity);

                impulseY += -thisInput.pitch() * (gamepad.config().invertGyroY ? -1 : 1);
                impulseX += (-thisInput.roll() + -thisInput.yaw()) * (gamepad.config().invertGyroX ? -1 : 1);
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

    private boolean isAiming(Player player) {
        return player.isUsingItem() && switch (player.getUseItem().getUseAnimation()) {
            case BOW, CROSSBOW, SPEAR, SPYGLASS -> true;
            default -> false;
        };
    }

    public record FunctionalLookInputModifier(BiFunction<Float, Controller<?, ?>, Float> x, BiFunction<Float, Controller<?, ?>, Float> y) implements LookInputModifier {
        @Override
        public float modifyX(float x, Controller<?, ?> controller) {
            return this.x.apply(x, controller);
        }

        @Override
        public float modifyY(float y, Controller<?, ?> controller) {
            return this.y.apply(y, controller);
        }
    }
}
