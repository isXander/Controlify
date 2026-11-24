/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.impl.utils.ColorSpaces;

import java.util.function.IntConsumer;

/// Renders the light layers to the controller's RGB light, encoded as sRGB.
///
/// The layers are composited over the light's default color, so a layer that fades out
/// fades back to the default rather than to black.
public final class LightRenderer implements FeedbackRenderer {
	private final IntConsumer setColor;
	private final int baseColor;
	private final float baseRed, baseGreen, baseBlue;

	/// @param setColor  receives an sRGB `0xRRGGBB` color to show
	/// @param baseColor the sRGB `0xRRGGBB` color shown where no layer covers the light
	public LightRenderer(IntConsumer setColor, int baseColor) {
		this.setColor = setColor;
		this.baseColor = baseColor & 0xFFFFFF;
		this.baseRed = decode(baseColor >> 16);
		this.baseGreen = decode(baseColor >> 8);
		this.baseBlue = decode(baseColor);
	}

	@Override
	public void render(MixedFrame frame, float dt) {
		float alpha = frame.lightAlpha();
		if (alpha <= 0f) {
			this.silence();
			return;
		}
		float beneath = 1f - alpha;
		this.setColor.accept(encode(frame.lightRed() + this.baseRed * beneath) << 16
				| encode(frame.lightGreen() + this.baseGreen * beneath) << 8
				| encode(frame.lightBlue() + this.baseBlue * beneath));
	}

	@Override
	public void silence() {
		this.setColor.accept(this.baseColor);
	}

	private static float decode(int channel) {
		return ColorSpaces.srgbToLinear((channel & 0xFF) / 255f);
	}

	private static int encode(float linear) {
		return Math.round(ColorSpaces.linearToSrgb(Math.clamp(linear, 0f, 1f)) * 255f);
	}
}
