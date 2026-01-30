package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.screenop.keyboard.KeyboardWidget;
import dev.isxander.controlify.utils.LazyComponentDims;
import dev.isxander.controlify.utils.PrecomputedComponentDims;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AbstractSignEditScreenProcessor extends ScreenProcessor<AbstractSignEditScreen> {

    private static final Component signLineHint = Component.translatable("controlify.hint.sign_line_change",
            ControlifyBindings.GUI_SECONDARY_NAVI_UP.inputGlyph(),
            ControlifyBindings.GUI_SECONDARY_NAVI_DOWN.inputGlyph());

    private final Consumer<Integer> moveCursorFunc;
    private final Supplier<SignBlockEntity> signSupplier;
    private final Supplier<KeyboardWidget> keyboardWidgetSupplier;

    private List<PrecomputedComponentDims<FormattedCharSequence>> signLineHintLines;

    public AbstractSignEditScreenProcessor(
            AbstractSignEditScreen screen,
            Consumer<Integer> moveCursorFunc,
            Supplier<SignBlockEntity> signSupplier,
            Supplier<KeyboardWidget> keyboardWidgetSupplier
    ) {
        super(screen);
        this.moveCursorFunc = moveCursorFunc;
        this.signSupplier = signSupplier;
        this.keyboardWidgetSupplier = keyboardWidgetSupplier;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);

        var config = controller.settings().generic;

        // move cursor down a line
        if (ControlifyBindings.GUI_SECONDARY_NAVI_DOWN.on(controller).justPressed()) {
            this.moveCursorFunc.accept(1);

            if (config.keyboard.hintSignLine && config.guide.showScreenGuides) {
                config.keyboard.hintSignLine = false;
                Controlify.instance().config().saveSafely();
            }

            playFocusChangeSound();
        }

        // move cursor up a line
        if (ControlifyBindings.GUI_SECONDARY_NAVI_UP.on(controller).justPressed()) {
            this.moveCursorFunc.accept(-1);

            if (config.keyboard.hintSignLine && config.guide.showScreenGuides) {
                config.keyboard.hintSignLine = false;
                Controlify.instance().config().saveSafely();
            }

            playFocusChangeSound();
        }
    }

    @Override
    protected void render(ControllerEntity controller, GuiGraphics graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {
        var config = controller.settings().generic;
        KeyboardWidget keyboardWidget = this.keyboardWidgetSupplier.get();
        if (keyboardWidget != null && config.guide.showScreenGuides) {
            if (config.keyboard.hintCursor) {
                LazyComponentDims hint = CommonKeyboardHints.TEXT_CURSOR;

                int x = keyboardWidget.getRight() - hint.getWidth() - 2;
                int y = keyboardWidget.getY() - hint.getHeight();

                graphics.drawString(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
            }

            if (config.keyboard.hintSignLine && this.signLineHintLines != null) {
                int y = 4;
                for (PrecomputedComponentDims<FormattedCharSequence> line : this.signLineHintLines) {
                    int lineWidth = line.width();
                    int lineHeight = line.height();
                    FormattedCharSequence lineText = line.component();

                    graphics.drawString(minecraft.font, lineText, this.screen.width - 1 - lineWidth, y, 0xFFFFFFFF, true);

                    y += minecraft.font.lineHeight;
                }
            }
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        if (Controlify.instance().currentInputMode() == InputMode.MIXED) {
            holdRepeatHelper.clearDelay();
        }

        getWidget(CommonComponents.GUI_DONE).ifPresent(doneButton ->
                ButtonGuideApi.addGuideToButton(
                        (AbstractButton) doneButton,
                        ControlifyBindings.GUI_BACK,
                        ButtonGuidePredicate.always()
                ));

        // compute hint wrapping
        SignBlockEntity sign = this.signSupplier.get();
        int signRight = (screen.width / 2) + (sign.getMaxTextLineWidth() / 2);
        int maxLineWidth = screen.width - signRight;
        this.signLineHintLines = minecraft.font.split(signLineHint, maxLineWidth).stream()
                .map(cs -> PrecomputedComponentDims.of(cs, minecraft.font))
                .toList();
    }
}
