package dev.isxander.controlify.screenop.compat;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.utils.HoldRepeatHelper;

/**
 * A component processor that handles incrementing and decrementing a slider.
 * This uses {@link dev.isxander.controlify.bindings.ControllerBindings#CYCLE_OPT_FORWARD} and {@link dev.isxander.controlify.bindings.ControllerBindings#CYCLE_OPT_BACKWARD} to increment and decrement the slider.
 */
public abstract class AbstractSliderComponentProcessor implements ComponentProcessor {
    private final HoldRepeatHelper holdRepeatHelper = new HoldRepeatHelper(15, 3);

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerEntity controller) {
        var left = ControlifyBindings.GUI_SECONDARY_NAVI_LEFT.on(controller).digitalNow();
        var leftPrev = ControlifyBindings.GUI_SECONDARY_NAVI_LEFT.on(controller).digitalPrev();
        var right = ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT.on(controller).digitalNow();
        var rightPrev = ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT.on(controller).digitalPrev();

        boolean repeatEventAvailable = holdRepeatHelper.canNavigate();

        if (left && (repeatEventAvailable || !leftPrev)) {
            incrementSlider(true);

            if (!leftPrev)
                holdRepeatHelper.reset();
        } else if (right && (repeatEventAvailable || !rightPrev)) {
            incrementSlider(false);

            if (!rightPrev)
                holdRepeatHelper.reset();
        } else {
            return false;
        }

        holdRepeatHelper.onNavigate();

        return true;
    }

    protected abstract void incrementSlider(boolean reverse);
}
