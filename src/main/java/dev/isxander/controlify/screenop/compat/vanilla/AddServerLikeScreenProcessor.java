package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.keyboard.KeyboardLayouts;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

public class AddServerLikeScreenProcessor extends ScreenProcessor<Screen> {

    private final Supplier<EditBox> ipEditBoxSupplier;
    private final Supplier<Button> doneButtonSupplier;
    private final Supplier<Button> backButtonSupplier;

    public AddServerLikeScreenProcessor(
            Screen screen,
            Supplier<EditBox> ipEditBoxSupplier,
            Supplier<Button> doneButtonSupplier,
            Supplier<Button> backButtonSupplier
    ) {
        super(screen);
        this.ipEditBoxSupplier = ipEditBoxSupplier;
        this.doneButtonSupplier = doneButtonSupplier;
        this.backButtonSupplier = backButtonSupplier;
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        EditBox ipEditBox = ipEditBoxSupplier.get();
        Button doneButton = doneButtonSupplier.get();
        Button backButton = backButtonSupplier.get();

        if (ipEditBox != null) {
            var processor = (EditBoxComponentProcessor) ComponentProcessorProvider.provide(ipEditBox);
            processor.setKeyboardLayout(KeyboardLayouts.serverIp());
        }

        if (doneButton != null) {
            ButtonGuideApi.addGuideToButton(
                    doneButton,
                    () -> doneButton.isFocused()
                            ? ControlifyBindings.GUI_PRESS
                            : ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                    ButtonGuidePredicate.always()
            );
        }

        if (backButton != null) {
            ButtonGuideApi.addGuideToButton(
                    backButton,
                    () -> ControlifyBindings.GUI_BACK,
                    ButtonGuidePredicate.always()
            );
        }
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).guiPressed().get()) {
            Button doneButton = doneButtonSupplier.get();
            if (doneButton != null && !doneButton.isFocused()) {
                playClackSound();
                screen.setFocused(doneButton);
            }
        }

        super.handleButtons(controller);
    }
}
