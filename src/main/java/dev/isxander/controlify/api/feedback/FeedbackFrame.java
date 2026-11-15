/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

/// The neutral output signal of one sample of an effect.
///
/// Effects describe *what* should be felt; renderers decide how a given controller can
/// reproduce it. Amplitudes are 0 to 1. Writing the same band twice in one sample keeps the
/// larger value.
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

	/// Whether the controller currently reproduces [#voice] and [#click] faithfully, so an
	/// effect can add detail only where it can be felt.
	boolean hd();
}
