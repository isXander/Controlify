package dev.isxander.controlify.compatibility.vanilla;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screen.vanilla.JoinMultiplayerScreenAccessor;

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
