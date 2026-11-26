/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.gui.keyboard;

import dev.isxander.controlify.impl.gui.keyboard.AboveOrBelowWidgetPositioner;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Supplier;

@FunctionalInterface
public interface KeyboardPositioner {
	ScreenRectangle positionKeyboard(int screenWidth, int screenHeight);

	static KeyboardPositioner aboveOrBelowWidgetPositioner(int desiredKeyboardWidth, int desiredKeyboardHeight, int padding, Supplier<ScreenRectangle> widgetRectSupplier) {
		return new AboveOrBelowWidgetPositioner(desiredKeyboardWidth, desiredKeyboardHeight, padding, widgetRectSupplier);
	}
}
