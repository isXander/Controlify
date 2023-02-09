package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screenop.vanilla.JoinMultiplayerScreenAccessor;

public class ServerSelectionListEntryComponentProcessor implements ComponentProcessor {
    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            screen.screen.setFocused(((JoinMultiplayerScreenAccessor) screen.screen).getSelectButton());
            return true;
        }

        return true;
    }
}
