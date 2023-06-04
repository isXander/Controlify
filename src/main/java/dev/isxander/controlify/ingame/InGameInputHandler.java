package dev.isxander.controlify.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.gamepad.GamepadController;
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
import net.minecraft.world.InteractionHand;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class InGameInputHandler {
    private final Controller<?, ?> controller;
    private final Minecraft minecraft;

    private double lookInputX, lookInputY;
    private boolean shouldShowPlayerList;

    private final NavigationHelper dropRepeatHelper;

    public InGameInputHandler(Controller<?, ?> controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();
        this.dropRepeatHelper = new NavigationHelper(20, 1);

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> {
            if (minecraft.player != null) {
                minecraft.player.input = mode == InputMode.CONTROLLER
                        ? new ControllerPlayerMovement(controller, minecraft.player)
                        : new KeyboardInput(minecraft.options);
            }
        });
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

            if (controller.bindings().INVENTORY.justPressed()) {
                if (minecraft.gameMode.isServerControlledInventory()) {
                    minecraft.player.sendOpenInventory();
                } else {
                    minecraft.getTutorial().onOpenInventory();
                    minecraft.setScreen(new InventoryScreen(minecraft.player));
                }
            }
        }
        if (controller.bindings().TOGGLE_HUD_VISIBILITY.justPressed()) {
            minecraft.options.hideGui = !minecraft.options.hideGui;
        }

        shouldShowPlayerList = controller.bindings().SHOW_PLAYER_LIST.held();
    }

    protected void handlePlayerLookInput() {
        var player = this.minecraft.player;
        var gamepad = controller instanceof GamepadController ? (GamepadController) controller : null;

        if (!minecraft.mouseHandler.isMouseGrabbed() || (!minecraft.isWindowActive() && !Controlify.instance().config().globalSettings().outOfFocusInput) || minecraft.screen != null || player == null) {
            lookInputX = 0;
            lookInputY = 0;
            return;
        }

        // flick stick - turn 90 degrees immediately upon turning
        // should be paired with gyro controls
        if (gamepad != null && gamepad.config().flickStick) {
            var turnAngle = 90 / 0.15f; // Entity#turn multiplies cursor delta by 0.15 to get rotation

            AtomicReference<Float> lastAngle = new AtomicReference<>(0f);
            Vector2fc flickVec = new Vector2f(
                    controller.bindings().LOOK_RIGHT.justPressed() ? 1 : controller.bindings().LOOK_LEFT.justPressed() ? -1 : 0,
                    controller.bindings().LOOK_DOWN.justPressed() ? 1 : controller.bindings().LOOK_UP.justPressed() ? -1 : 0
            );

            if (!flickVec.equals(0, 0)) {
                Animator.INSTANCE.play(new Animator.AnimationInstance(10, Easings::easeOutExpo)
                        .addConsumer(angle -> {
                            player.turn((angle - lastAngle.get()) * flickVec.x(), (angle - lastAngle.get()) * flickVec.y());
                            lastAngle.set(angle);
                        }, 0, turnAngle));
            }

            return;
        }

        // normal look input
        var impulseY = controller.bindings().LOOK_DOWN.state() - controller.bindings().LOOK_UP.state();
        var impulseX = controller.bindings().LOOK_RIGHT.state() - controller.bindings().LOOK_LEFT.state();
        impulseX *= Math.abs(impulseX);
        impulseY *= Math.abs(impulseY);

        if (controller.config().reduceAimingSensitivity && player != null && player.isUsingItem()) {
            float aimMultiplier = switch (player.getUseItem().getUseAnimation()) {
                case BOW, CROSSBOW, SPEAR -> 0.6f;
                case SPYGLASS -> 0.2f;
                default -> 1f;
            };
            impulseX *= aimMultiplier;
            impulseY *= aimMultiplier;
        }

        // gyro input
        if (gamepad != null
                && gamepad.hasGyro()
                && (!gamepad.config().gyroRequiresButton || gamepad.bindings().GAMEPAD_GYRO_BUTTON.held())
        ) {
            var gyroDelta = gamepad.absoluteGyroState().deadzone(0.05f);

            impulseY += -gyroDelta.pitch() * gamepad.config().gyroLookSensitivity * (gamepad.config().invertGyroY ? -1 : 1);
            impulseX += (-gyroDelta.roll() + -gyroDelta.yaw()) * gamepad.config().gyroLookSensitivity * (gamepad.config().invertGyroX ? -1 : 1);
        }

        LookInputModifier lookInputModifier = ControlifyEvents.LOOK_INPUT_MODIFIER.invoker();
        impulseX = lookInputModifier.modifyX(impulseX, controller);
        impulseY = lookInputModifier.modifyY(impulseY, controller);

        lookInputX = impulseX * controller.config().horizontalLookSensitivity * 65f;
        lookInputY = impulseY * controller.config().verticalLookSensitivity * 65f;
    }

    public void processPlayerLook(float deltaTime) {
        if (minecraft.player != null) {
            minecraft.player.turn(lookInputX * deltaTime, lookInputY * deltaTime);
        }
    }

    public boolean shouldShowPlayerList() {
        return this.shouldShowPlayerList;
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
