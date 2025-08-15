package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.CommonComponents;

import java.util.Optional;
import java.util.function.Consumer;

public class AbstractSignEditScreenProcessor extends ScreenProcessor<AbstractSignEditScreen> {

    private final Consumer<Integer> moveCursorFunc;

    public AbstractSignEditScreenProcessor(
            AbstractSignEditScreen screen,
            Consumer<Integer> moveCursorFunc
    ) {
        super(screen);
        this.moveCursorFunc = moveCursorFunc;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);

        // move cursor down a line
        if (ControlifyBindings.GUI_SECONDARY_NAVI_DOWN.on(controller).justPressed()) {
            this.moveCursorFunc.accept(1);

            playFocusChangeSound();
        }

        // move cursor up a line
        if (ControlifyBindings.GUI_SECONDARY_NAVI_UP.on(controller).justPressed()) {
            this.moveCursorFunc.accept(-1);

            playFocusChangeSound();
        }
    }

    @Override
    protected void render(ControllerEntity controller, GuiGraphics graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {

    }

    @Override
    protected void setInitialFocus() {
        if (Controlify.instance().currentInputMode() == InputMode.MIXED) {
            holdRepeatHelper.clearDelay();
        } else {
            super.setInitialFocus();
        }
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
