package dev.isxander.controlify.screenop.compat;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.utils.NavigationHelper;

/**
 * A component processor that handles incrementing and decrementing a slider.
 * This uses {@link dev.isxander.controlify.bindings.ControllerBindings#CYCLE_OPT_FORWARD} and {@link dev.isxander.controlify.bindings.ControllerBindings#CYCLE_OPT_BACKWARD} to increment and decrement the slider.
 */
public abstract class AbstractSliderComponentProcessor implements ComponentProcessor {
    private final NavigationHelper navigationHelper = new NavigationHelper(15, 3);

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var leftPrev = controller.bindings().CYCLE_OPT_BACKWARD.prevHeld();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();
        var rightPrev = controller.bindings().CYCLE_OPT_FORWARD.prevHeld();

        boolean repeatEventAvailable = navigationHelper.canNavigate();

        if (left && (repeatEventAvailable || !leftPrev)) {
            incrementSlider(true);

            if (!leftPrev)
                navigationHelper.reset();
        } else if (right && (repeatEventAvailable || !rightPrev)) {
            incrementSlider(false);

            if (!rightPrev)
                navigationHelper.reset();
        } else {
            return false;
        }

        navigationHelper.onNavigate();

        return true;
    }

    protected abstract void incrementSlider(boolean reverse);
}
