/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ScreenRenderEvent {
	void onRender(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a);
}
