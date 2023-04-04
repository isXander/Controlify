package dev.isxander.controlify.ingame;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.event.ControlifyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;

public class InGameInputHandler {
    private final Controller<?, ?> controller;
    private final Minecraft minecraft;

    private double lookInputX, lookInputY;

    public InGameInputHandler(Controller<?, ?> controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();

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
        if (Minecraft.getInstance().screen != null)
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
                if (controller.bindings().DROP.justPressed()) {
                    minecraft.player.drop(false);
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
                }
            }

        }
        if (controller.bindings().TOGGLE_HUD_VISIBILITY.justPressed()) {
            minecraft.options.hideGui = !minecraft.options.hideGui;
        }
    }

    protected void handlePlayerLookInput() {
        var player = this.minecraft.player;

        var impulseY = controller.bindings().LOOK_DOWN.state() - controller.bindings().LOOK_UP.state();
        var impulseX = controller.bindings().LOOK_RIGHT.state() - controller.bindings().LOOK_LEFT.state();

        if (minecraft.mouseHandler.isMouseGrabbed() && minecraft.isWindowActive() && player != null) {
            lookInputX = impulseX * Math.abs(impulseX) * controller.config().horizontalLookSensitivity;
            lookInputY = impulseY * Math.abs(impulseY) * controller.config().verticalLookSensitivity;

            if (controller.config().reduceAimingSensitivity && player.isUsingItem()) {
                float aimMultiplier = switch (player.getUseItem().getUseAnimation()) {
                    case BOW, CROSSBOW, SPEAR -> 0.6f;
                    case SPYGLASS -> 0.2f;
                    default -> 1f;
                };
                lookInputX *= aimMultiplier;
                lookInputY *= aimMultiplier;
            }
        } else {
            lookInputX = lookInputY = 0;
        }
    }

    public void processPlayerLook(float deltaTime) {
        if (minecraft.player != null) {
            minecraft.player.turn(lookInputX * 65f * deltaTime, lookInputY * 65f * deltaTime);
        }
    }
}
