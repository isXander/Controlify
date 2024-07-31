package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.PauseScreenAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens./*? if >1.20.6 >>*/options. OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;

import java.util.function.Supplier;

public class PauseScreenProcessor extends ScreenProcessor<PauseScreen> {
    private final Supplier<Button> disconnectButtonSupplier;

    public PauseScreenProcessor(PauseScreen screen, Supplier<Button> disconnectButtonSupplier) {
        super(screen);
        this.disconnectButtonSupplier = disconnectButtonSupplier;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);

        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            minecraft.setScreen(new OptionsScreen(screen, minecraft.options));
        }
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).justPressed()) {
            screen.setFocused(disconnectButtonSupplier.get());
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        if (((PauseScreenAccessor) screen).getShowPauseMenu()) {
            ButtonGuideApi.addGuideToButton(
                    (AbstractButton) getWidget("menu.returnToGame").orElseThrow(),
                    ControlifyBindings.GUI_BACK,
                    ButtonGuidePredicate.always()
            );
            ButtonGuideApi.addGuideToButton(
                    (AbstractButton) getWidget("menu.options").orElseThrow(),
                    ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                    ButtonGuidePredicate.always()
            );
            ButtonGuideApi.addGuideToButton(
                    disconnectButtonSupplier.get(),
                    () -> disconnectButtonSupplier.get().isFocused()
                            ? ControlifyBindings.GUI_PRESS
                            : ControlifyBindings.GUI_ABSTRACT_ACTION_2,
                    ButtonGuidePredicate.always()
            );
        }
    }
}
