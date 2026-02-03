package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

import java.util.function.Supplier;

public class OptionsSubScreenProcessor<T extends OptionsSubScreen> extends ScreenProcessor<T> {
    private final Supplier<Button> doneButtonSupplier;

    public OptionsSubScreenProcessor(T screen, Supplier<Button> doneButtonSupplier) {
        super(screen);
        this.doneButtonSupplier = doneButtonSupplier;
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        Button doneButton = doneButtonSupplier.get();
        if (doneButton != null) {
            ButtonGuideApi.addGuideToButton(
                    doneButton,
                    ControlifyBindings.GUI_BACK,
                    ButtonGuidePredicate.always()
            );
        }
    }
}
