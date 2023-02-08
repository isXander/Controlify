package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.yacl.gui.controllers.cycling.CyclingControllerElement;

public class CyclingControllerElementComponentProcessor implements ComponentProcessor {
    private final CyclingControllerElement cyclingController;
    private int lastInput = 0;

    public CyclingControllerElementComponentProcessor(CyclingControllerElement cyclingController) {
        this.cyclingController = cyclingController;
    }

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, Controller controller) {
        var rightStickX = controller.state().axes().rightStickX();
        var rightStickY = controller.state().axes().rightStickY();
        var input = Math.abs(rightStickX) > Math.abs(rightStickY) ? rightStickX : rightStickY;

        var inputI = Math.abs(input) < controller.config().buttonActivationThreshold ? 0 : input > 0 ? 1 : -1;
        if (inputI != 0 && inputI != lastInput) {
            cyclingController.cycleValue(input > 0 ? 1 : -1);

            lastInput = inputI;
            return true;
        }
        lastInput = inputI;

        return false;
    }
}
