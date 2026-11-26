/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.controllers;

import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/// Draws a controller type's icon sprite, scaled to fit the description width.
/// Controller sprites are square, so the height used equals the drawn width.
public class ControllerSpriteImageRenderer implements ImageRenderer {
	private static final int MAX_SIZE = 96;

	private final Identifier sprite;

	public ControllerSpriteImageRenderer(Identifier sprite) {
		this.sprite = sprite;
	}

	@Override
	public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
		int size = Math.max(1, Math.min(renderWidth, MAX_SIZE));
		int drawX = x + (renderWidth - size) / 2;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, drawX, y, size, size);
		return size;
	}

	@Override
	public void close() {
	}
}
