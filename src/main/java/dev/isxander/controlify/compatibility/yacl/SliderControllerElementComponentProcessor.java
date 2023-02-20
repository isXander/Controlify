package dev.isxander.controlify.compatibility.yacl;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.yacl.gui.controllers.slider.SliderControllerElement;

public class SliderControllerElementComponentProcessor implements ComponentProcessor {
    private final SliderControllerElement slider;
    private int ticksSinceIncrement = 0;
    private boolean prevLeft, prevRight;

    public SliderControllerElementComponentProcessor(SliderControllerElement element) {
        this.slider = element;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        ticksSinceIncrement++;

        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();

        if (left || right) {
            if (ticksSinceIncrement > controller.config().screenRepeatNavigationDelay || left != prevLeft || right != prevRight) {
                slider.incrementValue(left ? -1 : 1);
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
}
