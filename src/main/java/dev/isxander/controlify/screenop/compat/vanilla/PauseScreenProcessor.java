package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;

import java.util.function.Supplier;

public class PauseScreenProcessor extends ScreenProcessor<PauseScreen> {
    private final Supplier<Button> disconnectButtonSupplier;
    private final boolean showButtons;

    public PauseScreenProcessor(PauseScreen screen, boolean showButtons, Supplier<Button> disconnectButtonSupplier) {
        super(screen);
        this.showButtons = showButtons;
        this.disconnectButtonSupplier = disconnectButtonSupplier;
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        super.handleButtons(controller);

        if (controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) {
            minecraft.setScreen(new OptionsScreen(screen, minecraft.options));
        }
        if (controller.bindings().GUI_ABSTRACT_ACTION_2.justPressed()) {
            disconnectButtonSupplier.get().onPress();
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        if (showButtons) {
            ButtonGuideApi.addGuideToButtonBuiltin(
                    (AbstractButton) getWidget("menu.returnToGame").orElseThrow(),
                    bindings -> bindings.GUI_BACK,
                    ButtonRenderPosition.TEXT,
                    ButtonGuidePredicate.ALWAYS
            );
            ButtonGuideApi.addGuideToButtonBuiltin(
                    (AbstractButton) getWidget("menu.options").orElseThrow(),
                    bindings -> bindings.GUI_ABSTRACT_ACTION_1,
                    ButtonRenderPosition.TEXT,
                    ButtonGuidePredicate.ALWAYS
            );
            ButtonGuideApi.addGuideToButtonBuiltin(
                    disconnectButtonSupplier.get(),
                    bindings -> bindings.GUI_ABSTRACT_ACTION_2,
                    ButtonRenderPosition.TEXT,
                    ButtonGuidePredicate.ALWAYS
            );
        }
    }
}
