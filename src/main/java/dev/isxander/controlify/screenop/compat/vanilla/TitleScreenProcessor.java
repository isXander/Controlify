package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens./*? if >1.20.6 >>*/options. OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class TitleScreenProcessor extends ScreenProcessor<TitleScreen> {
    public TitleScreenProcessor(TitleScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
            screen.setFocused(getWidget("menu.quit").orElseThrow());
            playClackSound();
        }

        super.handleButtons(controller);

        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            minecraft.setScreen(new OptionsScreen(screen, minecraft.options));
            playClackSound();
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        AbstractButton quitButton = (AbstractButton) getWidget("menu.quit").orElseThrow();
        ButtonGuideApi.addGuideToButton(
                quitButton,
                () -> quitButton.isFocused() ? ControlifyBindings.GUI_PRESS : ControlifyBindings.GUI_BACK,
                ButtonGuidePredicate.ALWAYS
        );
        ButtonGuideApi.addGuideToButton(
                (AbstractButton) getWidget("menu.options").orElseThrow(),
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.ALWAYS
        );
    }
}
