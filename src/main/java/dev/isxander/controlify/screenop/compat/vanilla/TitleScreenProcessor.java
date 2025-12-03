package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
//? if neoforge {
/*import dev.isxander.controlify.screenop.compat.neoforge.NeoForgeTitleScreenProcessorCompat;*/
//?}

public class TitleScreenProcessor extends ScreenProcessor<TitleScreen> {
    //? if neoforge {
    /*private final NeoForgeTitleScreenProcessorCompat neoForgeCompat;*/
    //?}
    public TitleScreenProcessor(TitleScreen screen) {
        super(screen);
        //? if neoforge {
        /*this.neoForgeCompat = new NeoForgeTitleScreenProcessorCompat(
                key -> (AbstractButton) getWidget(key).orElseThrow(),
                this
        );*/
        //?}
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
        //? if neoforge {
        /*neoForgeCompat.onHandleButtons(controller);*/
        //?}
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        AbstractButton quitButton = (AbstractButton) getWidget("menu.quit").orElseThrow();
        ButtonGuideApi.addGuideToButton(
                quitButton,
                () -> quitButton.isFocused() ? ControlifyBindings.GUI_PRESS : ControlifyBindings.GUI_BACK,
                ButtonGuidePredicate.always()
        );
        ButtonGuideApi.addGuideToButton(
                (AbstractButton) getWidget("menu.options").orElseThrow(),
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always()
        );
        //? if neoforge {
        /*neoForgeCompat.onAddGuides();*/
        //?}
    }
}
