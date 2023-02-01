package dev.isxander.controlify.ingame;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.player.KeyboardInput;

public class InGameInputHandler {
    private final Controller controller;
    private final Minecraft minecraft;

    private double accumulatedDX, accumulatedDY;
    private double deltaTime;

    public InGameInputHandler(Controller controller) {
        this.controller = controller;
        this.minecraft = Minecraft.getInstance();

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> {
            if (minecraft.player != null) {
                minecraft.player.input = mode == InputMode.CONTROLLER
                        ? new ControllerPlayerMovement(controller)
                        : new KeyboardInput(minecraft.options);
            }
        });
    }

    public void inputTick() {
        var axes = controller.state().axes();
        if (minecraft.mouseHandler.isMouseGrabbed() && minecraft.isWindowActive()) {
            accumulatedDX += axes.rightStickX();
            accumulatedDY += axes.rightStickY();
        }

        processPlayerLook();

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

    public void processPlayerLook() {
        var time = Blaze3D.getTime();
        var delta = time - deltaTime;
        deltaTime = time;

        var sensitivity = controller.config().lookSensitivity * 8f + 2f;
        var sensCubed = sensitivity * sensitivity * sensitivity;

        var dx = accumulatedDX * delta;
        var dy = accumulatedDY * delta;
        accumulatedDX -= dx * 20; // 20 is how quickly the camera will slow down
        accumulatedDY -= dy * 20;

        if (minecraft.player != null)
            minecraft.player.turn(dx * sensCubed, dy * sensCubed);
    }
}
