/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.controllers;

import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Supplier;

/// Option descriptions are only rebuilt when their own option changes, but binding conflicts
/// depend on every other binding. Rendering the lines each frame keeps them current.
public class BindingInfoImageRenderer implements ImageRenderer {
	private final Supplier<List<Component>> lines;

	public BindingInfoImageRenderer(Supplier<List<Component>> lines) {
		this.lines = lines;
	}

	@Override
	public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
		Font font = Minecraft.getInstance().font;
		int height = 0;

		List<Component> lines = this.lines.get();
		for (int i = 0; i < lines.size(); i++) {
			if (i == 0 || i == lines.size() - 1) {
				height += 5; // add padding
			}

			Component line = lines.get(i);

			for (FormattedCharSequence wrapped : font.split(line, renderWidth)) {
				graphics.text(font, wrapped, x, y + height, -1, true);
				height += font.lineHeight + 2;
			}
		}

		return height;
	}

	@Override
	public void close() {
	}
}
