package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;

public class SliderControllerElementComponentProcessor implements ComponentProcessor {
    private final SliderControllerElement slider;
    private final HoldRepeatHelper holdRepeatHelper = new HoldRepeatHelper(15, 3);

    public SliderControllerElementComponentProcessor(SliderControllerElement element) {
        this.slider = element;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var leftPrev = controller.bindings().CYCLE_OPT_BACKWARD.prevHeld();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();
        var rightPrev = controller.bindings().CYCLE_OPT_FORWARD.prevHeld();

        boolean repeatEventAvailable = holdRepeatHelper.canNavigate();

        if (left && (repeatEventAvailable || !leftPrev)) {
            slider.incrementValue(-1);

            if (!leftPrev)
                holdRepeatHelper.reset();
        } else if (right && (repeatEventAvailable || !rightPrev)) {
            slider.incrementValue(1);

            if (!rightPrev)
                holdRepeatHelper.reset();
        } else {
            return false;
        }

        holdRepeatHelper.onNavigate();

        return true;
    }
}
