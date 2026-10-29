/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

import dev.isxander.controlify.impl.gui.font.ComponentFontHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public record PrecomputedComponentDims<T>(T component, int width, int height) {
	public static PrecomputedComponentDims<Component> of(Component component, Font font) {
		int width = font.width(component);
		int height = ComponentFontHelper.getComponentHeight(font, component);
		return new PrecomputedComponentDims<>(component, width, height);
	}

	public static PrecomputedComponentDims<FormattedCharSequence> of(FormattedCharSequence component, Font font) {
		int width = font.width(component);
		int height = ComponentFontHelper.getComponentHeight(font, component);
		return new PrecomputedComponentDims<>(component, width, height);
	}
}
