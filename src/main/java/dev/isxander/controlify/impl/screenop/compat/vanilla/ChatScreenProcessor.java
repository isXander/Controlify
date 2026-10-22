/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.screenop.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.impl.screenop.keyboard.ComponentKeyboardBehaviour;
import dev.isxander.controlify.impl.screenop.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.utils.LazyComponentDims;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class ChatScreenProcessor extends ScreenProcessor<ChatScreen> {

	private final Supplier<EditBox> inputSupplier;
	private final Supplier<@Nullable KeyboardWidget> keyboardSupplier;
	private final Supplier<@Nullable CmdSuggestionsController> suggestionsController;

	private final LazyComponentDims commandSuggesterHint = new LazyComponentDims(
			Component.translatable("controlify.hint.command_suggester",
					ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_DOWN.actionId()),
					ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_UP.actionId()),
					ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.actionId()))
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
	protected void handleButtons(ControllerImpl controller) {
		super.handleButtons(controller);

		CmdSuggestionsController suggestionsController = this.suggestionsController.get();
		if (suggestionsController != null) {
			if (ControlifyActions.GUI_SECONDARY_NAVI_DOWN.on(controller).pulse().consumePulse()) {
				if (suggestionsController.controlify$cycle(1)) {

					playFocusChangeSound();

					this.clearCommandSuggesterHint(controller);
				}
			}
			if (ControlifyActions.GUI_SECONDARY_NAVI_UP.on(controller).pulse().consumePulse()) {
				if (suggestionsController.controlify$cycle(-1)) {

					playFocusChangeSound();

					this.clearCommandSuggesterHint(controller);
				}
			}
			if (ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.on(controller).pulse().consumePulse()) {
				if (suggestionsController.controlify$useSuggestion()) {

					playClackSound();

					this.clearCommandSuggesterHint(controller);
				}
			}
		}
	}

	@Override
	protected void render(ControllerImpl controller, GuiGraphicsExtractor graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {
		var settings = controller.settings().generic;

		if (this.keyboardSupplier.get() != null && settings.guide.showScreenGuides) {
			if (settings.keyboard.hintCursor) {
				LazyComponentDims hint = CommonKeyboardHints.TEXT_CURSOR;

				int x = this.inputSupplier.get().getRight() - hint.getWidth() - 2;
				int y = this.inputSupplier.get().getY() - hint.getHeight();

				graphics.text(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
			}

			if (settings.keyboard.hintCommandSuggester) {
				CmdSuggestionsController suggestionsController = this.suggestionsController.get();

				if (suggestionsController != null && suggestionsController.controlify$hasAvailableSuggestions()) {
					LazyComponentDims hint = this.commandSuggesterHint;

					int x = this.screen.width - hint.getWidth() - 2;
					int y = 2 + Math.max(0, hint.getHeight() - minecraft.font.lineHeight);

					graphics.text(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
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

	private void clearCommandSuggesterHint(ControllerImpl controller) {
		var settings = controller.settings().generic;
		if (settings.keyboard.hintCommandSuggester && settings.guide.showScreenGuides) {
			settings.keyboard.hintCommandSuggester = false;
			Controlify.instance().config().saveSafely();
		}
	}

	public interface CmdSuggestionsController {
		boolean controlify$cycle(int amount);

		boolean controlify$useSuggestion();

		boolean controlify$hasAvailableSuggestions();
	}
}
