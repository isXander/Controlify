package dev.isxander.controlify.screenop.compat;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;

/**
 * A component processor that handles incrementing and decrementing a slider.
 * This uses {@link dev.isxander.controlify.bindings.ControllerBindings#CYCLE_OPT_FORWARD} and {@link dev.isxander.controlify.bindings.ControllerBindings#CYCLE_OPT_BACKWARD} to increment and decrement the slider.
 */
public abstract class AbstractSliderComponentProcessor implements ComponentProcessor {
    private int ticksSinceIncrement = 0;
    private boolean prevLeft, prevRight;

    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        ticksSinceIncrement++;

        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();

        if (left || right) {
            if (ticksSinceIncrement > controller.config().screenRepeatNavigationDelay || left != prevLeft || right != prevRight) {
                incrementSlider(left);
                ticksSinceIncrement = 0;
                prevLeft = left;
                prevRight = right;
                return true;
            }
        } else {
            this.prevLeft = false;
            this.prevRight = false;
        }

        return false;
    }

    protected abstract void incrementSlider(boolean reverse);
}
