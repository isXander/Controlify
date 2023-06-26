package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class TitleScreenProcessor extends ScreenProcessor<TitleScreen> {
    public TitleScreenProcessor(TitleScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        if (controller.bindings().GUI_BACK.justPressed()) {
            screen.setFocused(getWidget("menu.quit").orElseThrow());
            playClackSound();
        }

        super.handleButtons(controller);

        if (controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) {
            minecraft.setScreen(new OptionsScreen(screen, minecraft.options));
            playClackSound();
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        AbstractButton quitButton = (AbstractButton) getWidget("menu.quit").orElseThrow();
        ButtonGuideApi.addGuideToButtonBuiltin(
                quitButton,
                bindings -> quitButton.isFocused() ? bindings.GUI_PRESS : bindings.GUI_BACK,
                ButtonRenderPosition.TEXT,
                ButtonGuidePredicate.ALWAYS
        );
        ButtonGuideApi.addGuideToButtonBuiltin(
                (AbstractButton) getWidget("menu.options").orElseThrow(),
                bindings -> bindings.GUI_ABSTRACT_ACTION_1,
                ButtonRenderPosition.TEXT,
                ButtonGuidePredicate.ALWAYS
        );
    }
}
