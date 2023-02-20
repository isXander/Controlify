package dev.isxander.controlify.compatibility.sodium;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;

import java.util.function.Consumer;

public class CycleControlProcessor implements ComponentProcessor {
    private final Consumer<Boolean> cycleMethod;

    public CycleControlProcessor(Consumer<Boolean> cycleMethod) {
        this.cycleMethod = cycleMethod;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        if (controller.bindings().CYCLE_OPT_FORWARD.justPressed() || controller.bindings().GUI_PRESS.justPressed()) {
            cycleMethod.accept(false);
            return true;
        }
        if (controller.bindings().CYCLE_OPT_BACKWARD.justPressed()) {
            cycleMethod.accept(true);
            return true;
        }

        return false;
    }
}
