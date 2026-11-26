/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface GuideInstance<C extends Context> extends ContextualInstance<C> {
	/// Extracts render state to the screen.
	///
	/// @param bottomAligned whether the guide is aligned to th bottom edge instead of the top edge
	/// @param textContrast whether to render a background behind guide text to improve contrast
	/// @param guiScale the gui scale of the guide; the same scale as vanilla options' gui scale,
	///                 or `-1` to use the default gui scale
	void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast, int guiScale);

	/// Extracts render state to the screen.
	/// Equivalent to [#extractRenderState(GuiGraphicsExtractor, boolean, boolean, int)], setting gui scale to `-1`
	///
	/// @param bottomAligned whether the guide is aligned to th bottom edge instead of the top edge
	/// @param textContrast whether to render a background behind guide text to improve contrast
	default void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast) {
		extractRenderState(graphics, bottomAligned, textContrast, -1);
	}

	/// Returns a [Renderable] that you can add to (Screens)[net.minecraft.client.gui.screens.Screen]
	/// that simply calls [#extractRenderState(GuiGraphicsExtractor, boolean, boolean, int)]
	/// with the options you specify.
	///
	/// @param bottomAligned whether the guide is aligned to th bottom edge instead of the top edge
	/// @param textContrast whether to render a background behind guide text to improve contrast
	/// @param guiScale the gui scale of the guide; the same scale as vanilla options' gui scale,
	///                 or -1 to use the default gui scale
	Renderable renderable(boolean bottomAligned, boolean textContrast, int guiScale);

	/// Returns a [Renderable] that you can add to (Screens)[net.minecraft.client.gui.screens.Screen]
	/// that simply calls [#extractRenderState(GuiGraphicsExtractor, boolean, boolean)]
	/// with the options you specify.
	///
	/// @param bottomAligned whether the guide is aligned to th bottom edge instead of the top edge
	/// @param textContrast whether to render a background behind guide text to improve contrast
	default Renderable renderable(boolean bottomAligned, boolean textContrast) {
		return renderable(bottomAligned, textContrast, -1);
	}
}
