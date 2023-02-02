package dev.isxander.controlify.compatibility.vanilla;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screen.vanilla.SelectWorldScreenAccessor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class WorldListEntryComponentProcessor implements ComponentProcessor {
    @Override
    public boolean overrideControllerButtons(ScreenProcessor screen, Controller controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            var selectWorldScreen = (SelectWorldScreen) screen.screen;
            selectWorldScreen.setFocused(((SelectWorldScreenAccessor) selectWorldScreen).getSelectButton());

            return true;
        }

        return false;
    }
}
