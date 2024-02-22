package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.SelectWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class SelectWorldScreenProcessor extends ScreenProcessor<SelectWorldScreen> {
    public SelectWorldScreenProcessor(SelectWorldScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) {
            this.playClackSound();
            CreateWorldScreen.openFresh(Minecraft.getInstance(), screen);
            return;
        }

        if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
            if (controller.bindings().GUI_BACK.justPressed()) {
                screen.setFocused(((SelectWorldScreenAccessor) screen).getList());
                return;
            }
        }

        super.handleButtons(controller);
    }
}
