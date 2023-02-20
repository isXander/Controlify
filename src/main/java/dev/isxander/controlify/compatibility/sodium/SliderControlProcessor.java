package dev.isxander.controlify.compatibility.sodium;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;

import java.util.function.Consumer;

public class SliderControlProcessor implements ComponentProcessor {
    private final Consumer<Boolean> cycleMethod;
    private int ticksSinceIncrement = 0;
    private boolean prevLeft, prevRight;

    public SliderControlProcessor(Consumer<Boolean> cycleMethod) {
        this.cycleMethod = cycleMethod;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        ticksSinceIncrement++;

        var left = controller.bindings().CYCLE_OPT_BACKWARD.held();
        var right = controller.bindings().CYCLE_OPT_FORWARD.held();

        if (left || right) {
            if (ticksSinceIncrement > controller.config().screenRepeatNavigationDelay || left != prevLeft || right != prevRight) {
                cycleMethod.accept(left);
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
