package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;

public class CyclingControllerElementComponentProcessor implements ComponentProcessor {
    private final CyclingControllerElement cyclingController;
    private boolean prevLeft, prevRight;


    public CyclingControllerElementComponentProcessor(CyclingControllerElement cyclingController) {
        this.cyclingController = cyclingController;
    }

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerEntity controller) {
        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();

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
