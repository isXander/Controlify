/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.gui.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.utils.LazyComponentDims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ChatScreenBehaviour implements ScreenBehaviour {
	private final Access access;
	private final ScreenContext<? extends ChatScreen> context;
	private final ChatScreen screen;
	private final Minecraft minecraft = Minecraft.getInstance();

	private final LazyComponentDims commandSuggesterHint = new LazyComponentDims(
			Component.translatable("controlify.hint.command_suggester",
					ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_DOWN.actionId()),
					ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_UP.actionId()),
					ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.actionId()))
	);

	public ChatScreenBehaviour(ScreenContext<? extends ChatScreen> context) {
		this.context = context;
		this.screen = context.screen();
		this.access = (Access) context.screen();
	}

	@Override
	public void handleInteraction(Controller input, Runnable next) {
		var controller = (ControllerImpl) input;

		CmdSuggestionsController suggestionsController = access.controlify$getSuggestionsController();
		if (suggestionsController != null) {
			while (ControlifyActions.GUI_SECONDARY_NAVI_DOWN.on(controller).pulse().consumePulse()) {
				if (suggestionsController.controlify$cycle(1)) {

					context.playFocusChangeSound();

					this.clearCommandSuggesterHint(controller);
				}
			}
			while (ControlifyActions.GUI_SECONDARY_NAVI_UP.on(controller).pulse().consumePulse()) {
				if (suggestionsController.controlify$cycle(-1)) {

					context.playFocusChangeSound();

					this.clearCommandSuggesterHint(controller);
				}
			}
			while (ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.on(controller).pulse().consumePulse()) {
				if (suggestionsController.controlify$useSuggestion()) {

					context.playClickSound();

					this.clearCommandSuggesterHint(controller);
				}
			}
		}
		next.run();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller input, Runnable next) {
		var controller = (ControllerImpl) input;
		var settings = controller.settings().generic;

		if (access.controlify$getKeyboard() != null && settings.guide.showScreenGuides) {
			if (settings.keyboard.hintCursor) {
				LazyComponentDims hint = CommonKeyboardHints.TEXT_CURSOR;

				int x = access.controlify$getInput().getRight() - hint.getWidth() - 2;
				int y = access.controlify$getInput().getY() - hint.getHeight();

				graphics.text(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
			}

			if (settings.keyboard.hintCommandSuggester) {
				CmdSuggestionsController suggestionsController = access.controlify$getSuggestionsController();

				if (suggestionsController != null && suggestionsController.controlify$hasAvailableSuggestions()) {
					LazyComponentDims hint = this.commandSuggesterHint;

					int x = this.screen.width - hint.getWidth() - 2;
					int y = 2 + Math.max(0, hint.getHeight() - minecraft.font.lineHeight);

					graphics.text(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
				}
			}
		}
		next.run();
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

	public interface Access {
		EditBox controlify$getInput();

		@Nullable
		KeyboardWidget controlify$getKeyboard();

		@Nullable
		CmdSuggestionsController controlify$getSuggestionsController();
	}
}
