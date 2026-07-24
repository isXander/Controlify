/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class FakePositionPlainTextButton extends PlainTextButton {
	private ScreenRectangle fakePosition;

	public FakePositionPlainTextButton(int x, int y, int width, int height, Component content, OnPress empty, Font font) {
		super(x, y, width, height, content, empty, font);
	}

	public FakePositionPlainTextButton(Component text, Font font, int x, int y, OnPress onPress) {
		this(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, onPress, font);
	}

	public void setFakePosition(ScreenRectangle fakePosition) {
		this.fakePosition = fakePosition;
	}

	@Override
	public @NotNull ScreenRectangle getRectangle() {
		return isFocused() ? super.getRectangle() : fakePosition;
	}
}
