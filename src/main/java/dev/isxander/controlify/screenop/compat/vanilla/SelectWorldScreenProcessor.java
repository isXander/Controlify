package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screenop.vanilla.SelectWorldScreenAccessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class SelectWorldScreenProcessor extends ScreenProcessor<SelectWorldScreen> {
    public SelectWorldScreenProcessor(SelectWorldScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(Controller controller) {
        if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
            if (controller.bindings().GUI_BACK.justPressed()) {
                screen.setFocused(((SelectWorldScreenAccessor) screen).getList());
                return;
            }
        }

        super.handleButtons(controller);
    }
}
