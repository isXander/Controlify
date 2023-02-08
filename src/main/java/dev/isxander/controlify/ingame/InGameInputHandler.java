package dev.isxander.controlify.ingame;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;

public class InGameInputHandler {
    private final Controller controller;
    private final Minecraft minecraft;

    private final Input controllerInput, keyboardInput;

    private double accumulatedDX, accumulatedDY;
    private double deltaTime;

    public InGameInputHandler(Controller controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();

        this.controllerInput = new ControllerPlayerMovement(controller, minecraft.player);
        this.keyboardInput = new KeyboardInput(minecraft.options);

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> {
            if (minecraft.player != null) {
                minecraft.player.input = mode == InputMode.CONTROLLER
                        ? this.controllerInput
                        : this.keyboardInput;
            }
        });
    }

    public void inputTick() {
        handlePlayerLookInput();
        handleKeybinds();
    }

    protected void handleKeybinds() {
        if (Minecraft.getInstance().screen != null && !Minecraft.getInstance().screen.passEvents)
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
        }
    }

    protected void handlePlayerLookInput() {
        var axes = controller.state().axes();
        if (minecraft.mouseHandler.isMouseGrabbed() && minecraft.isWindowActive()) {
            accumulatedDX += axes.rightStickX();
            accumulatedDY += axes.rightStickY();
        }

        processPlayerLook();
    }

    public void processPlayerLook() {
        var time = Blaze3D.getTime();
        var delta = time - deltaTime;
        deltaTime = time;

        var hsensitivity = controller.config().horizontalLookSensitivity * 9.6 + 2.0;
        var hsensCubed = hsensitivity * hsensitivity * hsensitivity;
        var vsensitivity = controller.config().verticalLookSensitivity * 9.6 + 2.0;
        var vsensCubed = vsensitivity * vsensitivity * vsensitivity;

        var dx = accumulatedDX * delta;
        var dy = accumulatedDY * delta;

        // drag
        if (accumulatedDX > 0) {
            accumulatedDX -= Math.min(dx * 20, accumulatedDX);
        } else if (accumulatedDX < 0) {
            accumulatedDX -= Math.max(dx * 20, accumulatedDX);
        }
        if (accumulatedDY > 0) {
            accumulatedDY -= Math.min(dy * 20, accumulatedDY);
        } else if (accumulatedDY < 0) {
            accumulatedDY -= Math.max(dy * 20, accumulatedDY);
        }

        if (minecraft.player != null)
            minecraft.player.turn(dx * hsensCubed, dy * vsensCubed);
    }
}
