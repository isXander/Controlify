/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

public interface GuideInstance<T extends FactCtx> {
	GuideDomain<T> domain();

	boolean update(T context, Font font);

	void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast, int guiScale);

	default void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast) {
		extractRenderState(graphics, bottomAligned, textContrast, -1);
	}

	Renderable renderable(boolean bottomAligned, boolean textContrast, int guiScale);

	default Renderable renderable(boolean bottomAligned, boolean textContrast) {
		return renderable(bottomAligned, textContrast, -1);
	}
}
