/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import org.jetbrains.annotations.ApiStatus;

/// The neutral output signal of one sample of an effect.
///
/// Effects describe *what* should be felt; renderers decide how a given controller can
/// reproduce it. Amplitudes are 0 to 1. Writing the same band twice in one sample keeps the
/// larger value. Light is the exception: see [#light(float, float, float, float)].
@ApiStatus.NonExtendable
public interface FeedbackFrame {
	/// Drives a vibration band at a site. Every controller can reproduce this.
	void band(Site site, Band band, float amp);

	/// Shorthand for both bands on the whole controller.
	default void rumble(float low, float high) {
		this.band(Site.BODY, Band.LOW, low);
		this.band(Site.BODY, Band.HIGH, high);
	}

	/// A sustained tone at a site. HD renderers reproduce the frequency; others fold the
	/// amplitude into the nearest band.
	void voice(Site site, float hz, float amp);

	/// A single transient at a site. Call it once, on the sample the transient happens
	/// (see [Playback#crossed]), not on every sample.
	///
	/// @param sharpness 0 is a dull thud, 1 is a crisp tick
	void click(Site site, float intensity, float sharpness);

	/// Paints a layer over the controller's light, on controllers that have one.
	///
	/// Unlike vibration, layers do not add up: each covers what is beneath it by `alpha`.
	/// Within one sample, later layers cover earlier ones; across playbacks, higher-priority
	/// playbacks cover lower ones. Beneath every layer is the light's default color. The user's strength settings and distance falloff make a layer more
	/// transparent, not darker.
	///
	/// @param red   0 to 1, sRGB
	/// @param green 0 to 1, sRGB
	/// @param blue  0 to 1, sRGB
	/// @param alpha 0 to 1, how much this layer covers what is beneath it
	void light(float red, float green, float blue, float alpha);

	/// [#light(float, float, float, float)] with an sRGB `0xRRGGBB` color.
	default void light(int rgb, float alpha) {
		this.light(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
	}

	/// Whether the controller currently reproduces [#voice] and [#click] faithfully, so an
	/// effect can add detail only where it can be felt.
	boolean hd();
}
