package dev.isxander.controlify.compatibility.yacl.screenop;

import dev.isxander.controlify.bindings.ControlifyBindings;
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
        var left = ControlifyBindings.CYCLE_OPT_BACKWARD.on(controller).digitalNow();
        var leftPrev = ControlifyBindings.CYCLE_OPT_BACKWARD.on(controller).digitalPrev();
        var right = ControlifyBindings.CYCLE_OPT_FORWARD.on(controller).digitalNow();
        var rightPrev = ControlifyBindings.CYCLE_OPT_FORWARD.on(controller).digitalPrev();

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
