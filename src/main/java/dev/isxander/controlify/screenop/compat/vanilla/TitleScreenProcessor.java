package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class TitleScreenProcessor extends ScreenProcessor<TitleScreen> {
    public TitleScreenProcessor(TitleScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
            getWidget("menu.quit").ifPresent(widget -> {
                screen.setFocused(widget);
                playClackSound();
            });
        }

        super.handleButtons(controller);

        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            if (getWidget("menu.options").isPresent()) {
                minecraft.setScreen(new OptionsScreen(screen, minecraft.options));
                playClackSound();
            }
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        getWidget("menu.quit").ifPresent(widget -> {
            var button = (AbstractButton) widget;
            ButtonGuideApi.addGuideToButton(
                    button,
                    () -> button.isFocused() ? ControlifyBindings.GUI_PRESS : ControlifyBindings.GUI_BACK,
                    ButtonGuidePredicate.always()
            );
        });

        getWidget("menu.options").ifPresent(widget -> {
            var button = (AbstractButton) widget;
            ButtonGuideApi.addGuideToButton(
                    button,
                    ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                    ButtonGuidePredicate.always()
            );
        });

    }
}
