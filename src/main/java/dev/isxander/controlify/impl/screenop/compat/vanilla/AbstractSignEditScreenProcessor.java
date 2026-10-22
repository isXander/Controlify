/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.screenop.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.impl.screenop.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.utils.LazyComponentDims;
import dev.isxander.controlify.impl.utils.PrecomputedComponentDims;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
			ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_UP.actionId()),
			ActionGlyphs.action(ControlifyActions.GUI_SECONDARY_NAVI_DOWN.actionId()));

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
	protected void handleButtons(ControllerImpl controller) {
		super.handleButtons(controller);

		var config = controller.settings().generic;

		// move cursor down a line
		if (ControlifyActions.GUI_SECONDARY_NAVI_DOWN.on(controller).pulse().consumePulse()) {
			this.moveCursorFunc.accept(1);

			if (config.keyboard.hintSignLine && config.guide.showScreenGuides) {
				config.keyboard.hintSignLine = false;
				Controlify.instance().config().saveSafely();
			}

			playFocusChangeSound();
		}

		// move cursor up a line
		if (ControlifyActions.GUI_SECONDARY_NAVI_UP.on(controller).pulse().consumePulse()) {
			this.moveCursorFunc.accept(-1);

			if (config.keyboard.hintSignLine && config.guide.showScreenGuides) {
				config.keyboard.hintSignLine = false;
				Controlify.instance().config().saveSafely();
			}

			playFocusChangeSound();
		}
	}

	@Override
	protected void render(ControllerImpl controller, GuiGraphicsExtractor graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {
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
					int lineHeight = line.height();
					FormattedCharSequence lineText = line.component();

					graphics.text(minecraft.font, lineText, this.screen.width - 1 - lineWidth, y, 0xFFFFFFFF, true);

					y += minecraft.font.lineHeight;
				}
			}
		}
	}

	@Override
	public void onWidgetRebuild() {
		super.onWidgetRebuild();

		getWidget(CommonComponents.GUI_DONE).ifPresent(doneButton ->
				ButtonGuideApi.addGuideToButton(
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
	}
}
