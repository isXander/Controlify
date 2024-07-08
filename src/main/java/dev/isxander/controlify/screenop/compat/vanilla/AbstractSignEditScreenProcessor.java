package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.CommonComponents;

public class AbstractSignEditScreenProcessor extends ScreenProcessor<AbstractSignEditScreen> {
    public AbstractSignEditScreenProcessor(AbstractSignEditScreen screen) {
        super(screen);
    }

    @Override
    protected void setInitialFocus() {
        if (Controlify.instance().currentInputMode() == InputMode.MIXED)
            holdRepeatHelper.clearDelay();
        else
            super.setInitialFocus();
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        getWidget(CommonComponents.GUI_DONE).ifPresent(doneButton ->
                ButtonGuideApi.addGuideToButton(
                        (AbstractButton) doneButton,
                        ControlifyBindings.GUI_BACK,
                        ButtonGuidePredicate.always()
                ));
    }
}
