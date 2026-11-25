/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/// A custom slider, not a vanilla `AbstractSliderButton`, so Controlify has no built-in support for it.
public class SensitivityBar extends AbstractWidget {
	private static final float STEP = 0.05f;

	public SensitivityBar(int x, int y, int width, int height) {
		super(x, y, width, height, Component.translatable("examplemod.settings.sensitivity"));
	}

	/// Moves the value one step in the given direction, -1 or +1.
	public void nudge(int direction) {
		BoatSettings.tiltSensitivity = Mth.clamp(BoatSettings.tiltSensitivity + direction * STEP, 0f, 1f);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		BoatSettings.tiltSensitivity = Mth.clamp((float) (event.x() - this.getX()) / this.getWidth(), 0f, 1f);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == InputConstants.KEY_LEFT || event.key() == InputConstants.KEY_RIGHT) {
			this.nudge(event.key() == InputConstants.KEY_LEFT ? -1 : 1);
			return true;
		}
		return false;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
		int filled = (int) (this.getWidth() * BoatSettings.tiltSensitivity);
		graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xFF202020);
		graphics.fill(this.getX(), this.getY(), this.getX() + filled, this.getBottom(), 0xFF4A90D9);
		if (this.isFocused()) {
			graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFFFFFFFF);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, this.getMessage());
	}
}
