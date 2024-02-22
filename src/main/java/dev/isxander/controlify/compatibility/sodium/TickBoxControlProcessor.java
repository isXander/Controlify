package dev.isxander.controlify.compatibility.sodium;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;

public class TickBoxControlProcessor implements ComponentProcessor {
    private final Runnable toggleMethod;

    public TickBoxControlProcessor(Runnable toggleMethod) {
        this.toggleMethod = toggleMethod;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            toggleMethod.run();
            return true;
        }
        if (controller.bindings().CYCLE_OPT_FORWARD.justPressed() || controller.bindings().CYCLE_OPT_BACKWARD.justPressed()) {
            toggleMethod.run();
            return true;
        }

        return false;
    }
}
