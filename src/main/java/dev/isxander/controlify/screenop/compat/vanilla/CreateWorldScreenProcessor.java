package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import java.util.function.Consumer;

public class CreateWorldScreenProcessor extends ScreenProcessor<CreateWorldScreen> {
    private final Consumer<Boolean> tabChangeMethod;

    public CreateWorldScreenProcessor(CreateWorldScreen screen, Consumer<Boolean> tabChangeMethod) {
        super(screen);
        this.tabChangeMethod = tabChangeMethod;
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        if (controller.bindings().GUI_NEXT_TAB.justPressed()) {
            tabChangeMethod.accept(false);
            return;
        }
        if (controller.bindings().GUI_PREV_TAB.justPressed()) {
            tabChangeMethod.accept(true);
            return;
        }

        super.handleButtons(controller);
    }
}
