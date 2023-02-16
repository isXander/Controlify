package dev.isxander.controlify.screenop.compat.yacl;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.yacl.gui.controllers.cycling.CyclingControllerElement;

public class CyclingControllerElementComponentProcessor implements ComponentProcessor {
    private final CyclingControllerElement cyclingController;
    private int lastInput = 0;
    private boolean prevLeft, prevRight;


    public CyclingControllerElementComponentProcessor(CyclingControllerElement cyclingController) {
        this.cyclingController = cyclingController;
    }

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        var left = controller.bindings().GUI_NAVI_LEFT.held();
        var right = controller.bindings().GUI_NAVI_RIGHT.held();

        if (left && !prevLeft) {
            prevLeft = true;
            prevRight = false;

            cyclingController.cycleValue(-1);

            return true;
        } else if (right && !prevRight) {
            prevLeft = false;
            prevRight = true;

            cyclingController.cycleValue(1);

            return true;
        } else {
            prevLeft = left;
            prevRight = right;

            return false;
        }
    }
}
