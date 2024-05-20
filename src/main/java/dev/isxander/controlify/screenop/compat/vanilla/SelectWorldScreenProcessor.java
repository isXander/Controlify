package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
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
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            this.playClackSound();
            CreateWorldScreen.openFresh(Minecraft.getInstance(), screen);
            return;
        }

        if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
            if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
                screen.setFocused(((SelectWorldScreenAccessor) screen).getList());
                return;
            }
        }

        super.handleButtons(controller);
    }
}
