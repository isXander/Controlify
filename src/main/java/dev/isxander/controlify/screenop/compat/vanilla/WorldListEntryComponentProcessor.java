package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.SelectWorldScreenAccessor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class WorldListEntryComponentProcessor implements ComponentProcessor {
    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?> controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            var selectWorldScreen = (SelectWorldScreen) screen.screen;
            selectWorldScreen.setFocused(((SelectWorldScreenAccessor) selectWorldScreen).getSelectButton());

            return true;
        }

        return false;
    }
}
