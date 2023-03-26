package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

public class CreateWorldScreenProcessor extends ScreenProcessor<CreateWorldScreen> {
    private final Runnable onCreateButton;

    public CreateWorldScreenProcessor(CreateWorldScreen screen, Runnable onCreateButton) {
        super(screen);
        this.onCreateButton = onCreateButton;
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        if (controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) {
            this.onCreateButton.run();
            this.playClackSound();
        }

        super.handleButtons(controller);
    }
}
