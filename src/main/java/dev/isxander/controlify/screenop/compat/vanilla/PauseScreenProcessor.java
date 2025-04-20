package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.PauseScreenAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PauseScreenProcessor extends ScreenProcessor<PauseScreen> {
    private final Supplier<@Nullable Button> disconnectButtonSupplier;

    public PauseScreenProcessor(PauseScreen screen, Supplier<@Nullable Button> disconnectButtonSupplier) {
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
            getWidget("menu.returnToGame").ifPresent(widget -> {
                ButtonGuideApi.addGuideToButton(
                        (AbstractButton) widget,
                        ControlifyBindings.GUI_BACK,
                        ButtonGuidePredicate.always()
                );
            });
            getWidget("menu.options").ifPresent( widget -> {
                ButtonGuideApi.addGuideToButton(
                        (AbstractButton) widget,
                        ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                        ButtonGuidePredicate.always()
                );
            });

            Button disconnectButton = disconnectButtonSupplier.get();
            if (disconnectButton != null) {
                ButtonGuideApi.addGuideToButton(
                        disconnectButton,
                        () -> disconnectButton.isFocused()
                                ? ControlifyBindings.GUI_PRESS
                                : ControlifyBindings.GUI_ABSTRACT_ACTION_2,
                        ButtonGuidePredicate.always()
                );
            }
        }
    }
}
