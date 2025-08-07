package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.screenkeyboard.KeyboardWidget;
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
    private final HoldRepeatHelper textFwdCursorHoldRepeatHelper = new HoldRepeatHelper(10, 2);
    private final HoldRepeatHelper textBwdCursorHoldRepeatHelper = new HoldRepeatHelper(10, 2);
    private final HoldRepeatHelper suggestionsFwdHoldRepeatHelper = new HoldRepeatHelper(10, 4);
    private final HoldRepeatHelper suggestionsBwdHoldRepeatHelper = new HoldRepeatHelper(10, 4);

    private final Supplier<EditBox> inputSupplier;
    private final Supplier<@Nullable KeyboardWidget> keyboardSupplier;
    private final Supplier<@Nullable CmdSuggestionsController> suggestionsController;

    private final LazyComponentDims cursorNavigateHint = new LazyComponentDims(
            Component.translatable("controlify.hint.chat_cursor_movement",
                    BindingFontHelper.binding(ControlifyBindings.GUI_PREV_TAB),
                    BindingFontHelper.binding(ControlifyBindings.GUI_NEXT_TAB))
    );
    private final LazyComponentDims commandSuggesterHint = new LazyComponentDims(
            Component.translatable("controlify.hint.command_suggester",
                    BindingFontHelper.binding(ControlifyBindings.GUI_SECONDARY_NAVI_DOWN),
                    BindingFontHelper.binding(ControlifyBindings.GUI_SECONDARY_NAVI_UP),
                    BindingFontHelper.binding(ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT))
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

        if (this.textFwdCursorHoldRepeatHelper.shouldAction(ControlifyBindings.GUI_NEXT_TAB.on(controller))) {
            this.inputSupplier.get().moveCursor(1, false);

            this.textFwdCursorHoldRepeatHelper.onNavigate();
            this.textBwdCursorHoldRepeatHelper.reset();

            playFocusChangeSound();

            this.clearChatCursorHint(controller);
        }

        if (this.textBwdCursorHoldRepeatHelper.shouldAction(ControlifyBindings.GUI_PREV_TAB.on(controller))) {
            this.inputSupplier.get().moveCursor(-1, false);

            this.textBwdCursorHoldRepeatHelper.onNavigate();
            this.textFwdCursorHoldRepeatHelper.reset();

            playFocusChangeSound();

            this.clearChatCursorHint(controller);
        }

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
        if (this.keyboardSupplier.get() != null) {
            var config = controller.genericConfig().config();

            if (config.hintChatCursorMovement) {
                LazyComponentDims hint = this.cursorNavigateHint;

                int x = this.inputSupplier.get().getRight() - hint.getWidth() - 2;
                int y = this.inputSupplier.get().getY() - hint.getHeight();

                graphics.drawString(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
            }

            if (config.hintCommandSuggester) {
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

    private void clearChatCursorHint(ControllerEntity controller) {
        if (controller.genericConfig().config().hintChatCursorMovement) {
            if (!this.inputSupplier.get().getValue().isEmpty()) {
                controller.genericConfig().config().hintChatCursorMovement = false;
                Controlify.instance().config().save();
            }
        }
    }

    private void clearCommandSuggesterHint(ControllerEntity controller) {
        if (controller.genericConfig().config().hintCommandSuggester) {
            controller.genericConfig().config().hintCommandSuggester = false;
            Controlify.instance().config().save();
        }
    }

    @Override
    protected HoldRepeatHelper createHoldRepeatHelper() {
        // make the initial delay way less
        return new HoldRepeatHelper(3, 4);
    }

    public interface CmdSuggestionsController {
        boolean controlify$cycle(int amount);

        boolean controlify$useSuggestion();

        boolean controlify$hasAvailableSuggestions();
    }
}
