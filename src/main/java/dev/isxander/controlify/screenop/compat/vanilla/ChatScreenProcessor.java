package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.keyboard.ComponentKeyboardBehaviour;
import dev.isxander.controlify.screenop.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.screenop.keyboard.KeyboardWidget;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import dev.isxander.controlify.utils.LazyComponentDims;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class ChatScreenProcessor extends ScreenProcessor<ChatScreen> {
    private final HoldRepeatHelper suggestionsFwdHoldRepeatHelper = new HoldRepeatHelper(10, 4);
    private final HoldRepeatHelper suggestionsBwdHoldRepeatHelper = new HoldRepeatHelper(10, 4);

    private final Supplier<EditBox> inputSupplier;
    private final Supplier<@Nullable KeyboardWidget> keyboardSupplier;
    private final Supplier<@Nullable CmdSuggestionsController> suggestionsController;

    private final LazyComponentDims commandSuggesterHint = new LazyComponentDims(
            Component.translatable("controlify.hint.command_suggester",
                    ControlifyBindings.GUI_SECONDARY_NAVI_DOWN.inputGlyph(),
                    ControlifyBindings.GUI_SECONDARY_NAVI_UP.inputGlyph(),
                    ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT.inputGlyph())
    );

    public ChatScreenProcessor(
            ChatScreen screen,
            Supplier<EditBox> inputSupplier,
            Supplier<@Nullable KeyboardWidget> keyboardSupplier,
            Supplier<@Nullable CmdSuggestionsController> suggestionsController
    ) {
        super(screen);
        this.inputSupplier = inputSupplier;
        this.keyboardSupplier = keyboardSupplier;
        this.suggestionsController = suggestionsController;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);

        CmdSuggestionsController suggestionsController = this.suggestionsController.get();
        if (suggestionsController != null) {
            if (this.suggestionsFwdHoldRepeatHelper.shouldAction(ControlifyBindings.GUI_SECONDARY_NAVI_DOWN.on(controller))) {
                if (suggestionsController.controlify$cycle(1)) {
                    this.suggestionsFwdHoldRepeatHelper.onNavigate();
                    this.suggestionsBwdHoldRepeatHelper.reset();

                    playFocusChangeSound();

                    this.clearCommandSuggesterHint(controller);
                }
            }
            if (this.suggestionsBwdHoldRepeatHelper.shouldAction(ControlifyBindings.GUI_SECONDARY_NAVI_UP.on(controller))) {
                if (suggestionsController.controlify$cycle(-1)) {
                    this.suggestionsBwdHoldRepeatHelper.onNavigate();
                    this.suggestionsFwdHoldRepeatHelper.reset();

                    playFocusChangeSound();

                    this.clearCommandSuggesterHint(controller);
                }
            }
            if (ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT.on(controller).justPressed()) {
                if (suggestionsController.controlify$useSuggestion()) {
                    this.suggestionsFwdHoldRepeatHelper.reset();
                    this.suggestionsBwdHoldRepeatHelper.reset();

                    playClackSound();

                    this.clearCommandSuggesterHint(controller);
                }
            }
        }
    }

    @Override
    protected void render(ControllerEntity controller, GuiGraphics graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {
        var settings = controller.settings().generic;

        if (this.keyboardSupplier.get() != null && settings.guide.showScreenGuides) {
            if (settings.keyboard.hintCursor) {
                LazyComponentDims hint = CommonKeyboardHints.TEXT_CURSOR;

                int x = this.inputSupplier.get().getRight() - hint.getWidth() - 2;
                int y = this.inputSupplier.get().getY() - hint.getHeight();

                graphics.drawString(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
            }

            if (settings.keyboard.hintCommandSuggester) {
                CmdSuggestionsController suggestionsController = this.suggestionsController.get();

                if (suggestionsController != null && suggestionsController.controlify$hasAvailableSuggestions()) {
                    LazyComponentDims hint = this.commandSuggesterHint;

                    int x = this.screen.width - hint.getWidth() - 2;
                    int y = 2 + Math.max(0, hint.getHeight() - minecraft.font.lineHeight);

                    graphics.drawString(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
                }
            }
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();

        // ensure that the default edit box keyboard behaviour is disabled
        EditBox input = this.inputSupplier.get();
        if (input != null) {
            var processor = (EditBoxComponentProcessor) ComponentProcessorProvider.provide(input);
            processor.setKeyboardBehaviour(new ComponentKeyboardBehaviour.DoNothing());
        }
    }

    private void clearCommandSuggesterHint(ControllerEntity controller) {
        var settings = controller.settings().generic;
        if (settings.keyboard.hintCommandSuggester && settings.guide.showScreenGuides) {
            settings.keyboard.hintCommandSuggester = false;
            Controlify.instance().config().saveSafely();
        }
    }

    @Override
    protected HoldRepeatHelper createHoldRepeatHelper() {
        // make the initial delay way less
        return new HoldRepeatHelper(5, 4);
    }

    public interface CmdSuggestionsController {
        boolean controlify$cycle(int amount);

        boolean controlify$useSuggestion();

        boolean controlify$hasAvailableSuggestions();
    }
}
