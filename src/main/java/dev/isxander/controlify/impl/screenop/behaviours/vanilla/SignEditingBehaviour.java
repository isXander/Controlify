/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.gui.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.screenop.ScreenWidgets;
import dev.isxander.controlify.impl.utils.LazyComponentDims;
import dev.isxander.controlify.impl.utils.PrecomputedComponentDims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SignEditingBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends AbstractSignEditScreen> context;
	private final AbstractSignEditScreen screen;
	private final Minecraft minecraft = Minecraft.getInstance();

	private static final Component signLineHint = Component.translatable("controlify.hint.sign_line_change",
			ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_UP.actionId()),
			ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_DOWN.actionId()));

	private final Consumer<Integer> moveCursorFunc;
	private final Supplier<SignBlockEntity> signSupplier;
	private final Supplier<KeyboardWidget> keyboardWidgetSupplier;

	private @org.jetbrains.annotations.Nullable List<PrecomputedComponentDims<FormattedCharSequence>> signLineHintLines;

	public SignEditingBehaviour(
			ScreenContext<? extends AbstractSignEditScreen> context,
			Consumer<Integer> moveCursorFunc,
			Supplier<SignBlockEntity> signSupplier,
			Supplier<KeyboardWidget> keyboardWidgetSupplier
	) {
		this.context = context;
		this.screen = context.screen();
		this.moveCursorFunc = moveCursorFunc;
		this.signSupplier = signSupplier;
		this.keyboardWidgetSupplier = keyboardWidgetSupplier;
	}

	@Override
	public void handleInteraction(Controller input, Runnable next) {
		var controller = (ControllerImpl) input;

		var config = controller.settings().generic;

		// move cursor down a line
		while (ControlifyActions.GUI_SECONDARY_NAVI_DOWN.on(controller).pulse().consumePulse()) {
			this.moveCursorFunc.accept(1);

			if (config.keyboard.hintSignLine && config.guide.showScreenGuides) {
				config.keyboard.hintSignLine = false;
				Controlify.instance().config().saveSafely();
			}

			context.playFocusChangeSound();
		}

		// move cursor up a line
		while (ControlifyActions.GUI_SECONDARY_NAVI_UP.on(controller).pulse().consumePulse()) {
			this.moveCursorFunc.accept(-1);

			if (config.keyboard.hintSignLine && config.guide.showScreenGuides) {
				config.keyboard.hintSignLine = false;
				Controlify.instance().config().saveSafely();
			}

			context.playFocusChangeSound();
		}
		next.run();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller input, Runnable next) {
		var controller = (ControllerImpl) input;
		var config = controller.settings().generic;
		KeyboardWidget keyboardWidget = this.keyboardWidgetSupplier.get();
		if (keyboardWidget != null && config.guide.showScreenGuides) {
			if (config.keyboard.hintCursor) {
				LazyComponentDims hint = CommonKeyboardHints.TEXT_CURSOR;

				int x = keyboardWidget.getRight() - hint.getWidth() - 2;
				int y = keyboardWidget.getY() - hint.getHeight();

				graphics.text(minecraft.font, hint.getComponent(), x, y, 0xFFFFFFFF, true);
			}

			if (config.keyboard.hintSignLine && this.signLineHintLines != null) {
				int y = 4;
				for (PrecomputedComponentDims<FormattedCharSequence> line : this.signLineHintLines) {
					int lineWidth = line.width();
					FormattedCharSequence lineText = line.component();

					graphics.text(minecraft.font, lineText, this.screen.width - 1 - lineWidth, y, 0xFFFFFFFF, true);

					y += minecraft.font.lineHeight;
				}
			}
		}
		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {

		ScreenWidgets.find(screen, CommonComponents.GUI_DONE).ifPresent(doneButton ->
				context.buttonGuide().addGuideToButton(
						(AbstractButton) doneButton,
						ControlifyActions.GUI_BACK,
						ButtonGuidePredicate.always()
				));

		// compute hint wrapping
		SignBlockEntity sign = this.signSupplier.get();
		int signRight = (screen.width / 2) + (sign.getMaxTextLineWidth() / 2);
		int maxLineWidth = screen.width - signRight;
		this.signLineHintLines = minecraft.font.split(signLineHint, maxLineWidth).stream()
				.map(cs -> PrecomputedComponentDims.of(cs, minecraft.font))
				.toList();
		next.run();
	}
}
