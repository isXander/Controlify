package dev.isxander.controlify.compatibility.sodium;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;

public class ButtonProcessor implements ComponentProcessor {
    private final Runnable action;

    public ButtonProcessor(Runnable action) {
        this.action = action != null ? action : () -> {};
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            action.run();
            return true;
        }

        return false;
    }
}
