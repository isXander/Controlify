/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.gui.keyboard;

import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Supplier;

@FunctionalInterface
public interface KeyboardPositioner {
	ScreenRectangle positionKeyboard(int screenWidth, int screenHeight);

	static KeyboardPositioner aboveOrBelowWidgetPositioner(int desiredKeyboardWidth, int desiredKeyboardHeight, int padding, Supplier<ScreenRectangle> widgetRectSupplier) {
		return (screenWidth, screenHeight) -> {
			ScreenRectangle widgetRect = widgetRectSupplier.get();

			int keyboardWidth = Math.min(desiredKeyboardWidth, screenWidth);
			int keyboardLeft = Math.clamp(
					widgetRect.getCenterInAxis(ScreenAxis.HORIZONTAL) - keyboardWidth / 2,
					0, screenWidth - keyboardWidth
			);

			int spaceBelow = screenHeight - widgetRect.bottom() - padding;
			int spaceAbove = widgetRect.top() - padding;

			// Determine the best vertical position for the keyboard.
			// 1. Prefer placing the keyboard below the widget, if it fits.
			// 2. If there's not enough space below, check space above.
			// 3. If neither fits fully, pick the side with more space and shrink the height.
			int keyboardHeight;
			boolean above;
			if (spaceBelow >= desiredKeyboardHeight) {
				keyboardHeight = desiredKeyboardHeight;
				above = false;
			} else if (spaceAbove >= desiredKeyboardHeight) {
				keyboardHeight = desiredKeyboardHeight;
				above = true;
			} else if (spaceBelow >= spaceAbove) {
				keyboardHeight = spaceBelow;
				above = false;
			} else {
				keyboardHeight = spaceAbove;
				above = true;
			}
			int keyboardTop = above
					? widgetRect.top() - keyboardHeight - padding
					: widgetRect.bottom() + padding;

			return new ScreenRectangle(
					keyboardLeft,
					keyboardTop,
					keyboardWidth,
					keyboardHeight
			);
		};
	}
}
