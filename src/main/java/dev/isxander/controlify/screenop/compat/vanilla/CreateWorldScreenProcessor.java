package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

public class CreateWorldScreenProcessor extends ScreenProcessor<CreateWorldScreen> {
    private final Runnable onCreateButton;

    public CreateWorldScreenProcessor(CreateWorldScreen screen, Runnable onCreateButton) {
        super(screen);
        this.onCreateButton = onCreateButton;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            this.onCreateButton.run();
            this.playClackSound();
        }

        super.handleButtons(controller);
    }
}
