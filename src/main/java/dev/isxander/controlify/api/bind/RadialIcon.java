/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.bind;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface RadialIcon {
	RadialIcon EMPTY = (graphics, x, y, tickDelta) -> {};

	void draw(GuiGraphicsExtractor graphics, int x, int y, float tickDelta);
}
