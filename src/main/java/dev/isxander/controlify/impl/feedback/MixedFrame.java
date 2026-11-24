/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.FeedbackFrame;
import dev.isxander.controlify.api.feedback.Site;
import dev.isxander.controlify.impl.utils.ColorSpaces;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/// A set of bands, voices and clicks. Used both as the [FeedbackFrame] one effect writes a
/// sample into, and as the mix of every playback that renderers read.
///
/// Bands mix by maximum. Voices at the same site and frequency merge by maximum; when there
/// are more than [#MAX_VOICES], those of the lowest-priority playbacks are dropped first, then
/// the quietest. Clicks are kept individually, up to [#MAX_CLICKS] per frame. Light layers are
/// composited "over" each other in linear light, in the order they are written.
///
/// The engine mixes playbacks from lowest to highest priority, so later samples are on top.
public final class MixedFrame implements FeedbackFrame {
	public static final int MAX_VOICES = 8;
	public static final int MAX_CLICKS = 8;

	private static final Site[] SITES = Site.values();
	private static final int BANDS = Band.values().length;

	private final float[] bands = new float[SITES.length * BANDS];

	private final Site[] voiceSite = new Site[MAX_VOICES];
	private final float[] voiceHz = new float[MAX_VOICES];
	private final float[] voiceAmp = new float[MAX_VOICES];
	/// Which sample mixed each voice in: later samples have higher priority.
	private final int[] voicePriority = new int[MAX_VOICES];
	private int voices;
	/// The priority of voices written now: incremented for every sample mixed in.
	private int priority;

	private final Site[] clickSite = new Site[MAX_CLICKS];
	private final float[] clickIntensity = new float[MAX_CLICKS];
	private final float[] clickSharpness = new float[MAX_CLICKS];
	private int clicks;

	/// The composited light, in linear sRGB premultiplied by [#lightAlpha], which is also the
	/// light composited over black.
	private float lightRed, lightGreen, lightBlue, lightAlpha;

	private boolean hd;

	@Override
	public void band(@NonNull Site site, @NonNull Band band, float amp) {
		int i = site.ordinal() * BANDS + band.ordinal();
		this.bands[i] = Math.max(this.bands[i], clampAmp(amp));
	}

	@Override
	public void voice(@NonNull Site site, float hz, float amp) {
		amp = clampAmp(amp);
		if (amp <= 0f || !(hz > 0f) || !Float.isFinite(hz)) {
			return;
		}
		for (int i = 0; i < this.voices; i++) {
			if (this.voiceSite[i] == site && Math.abs(this.voiceHz[i] - hz) < 0.5f) {
				this.voiceAmp[i] = Math.max(this.voiceAmp[i], amp);
				this.voicePriority[i] = Math.max(this.voicePriority[i], this.priority);
				return;
			}
		}
		int slot = this.voices;
		if (slot == MAX_VOICES) {
			// full: replace the lowest-priority, then quietest, voice if this one outranks it
			slot = 0;
			for (int i = 1; i < MAX_VOICES; i++) {
				// voice i is weaker than the weakest so far
				if (this.outranks(i, this.voicePriority[slot], this.voiceAmp[slot])) {
					slot = i;
				}
			}
			if (!this.outranks(slot, this.priority, amp)) {
				return;
			}
		} else {
			this.voices++;
		}
		this.voiceSite[slot] = site;
		this.voiceHz[slot] = hz;
		this.voiceAmp[slot] = amp;
		this.voicePriority[slot] = this.priority;
	}

	/// Whether a voice of `priority` and `amp` should be kept over the voice in `slot`.
	private boolean outranks(int slot, int priority, float amp) {
		return priority != this.voicePriority[slot] ? priority > this.voicePriority[slot] : amp > this.voiceAmp[slot];
	}

	@Override
	public void click(@NonNull Site site, float intensity, float sharpness) {
		intensity = clampAmp(intensity);
		if (intensity <= 0f || this.clicks == MAX_CLICKS) {
			return;
		}
		this.clickSite[this.clicks] = site;
		this.clickIntensity[this.clicks] = intensity;
		this.clickSharpness[this.clicks] = clampAmp(sharpness);
		this.clicks++;
	}

	@Override
	public void light(float red, float green, float blue, float alpha) {
		alpha = clampAmp(alpha);
		if (alpha <= 0f) {
			return;
		}
		this.over(
				ColorSpaces.srgbToLinear(clampAmp(red)) * alpha,
				ColorSpaces.srgbToLinear(clampAmp(green)) * alpha,
				ColorSpaces.srgbToLinear(clampAmp(blue)) * alpha,
				alpha
		);
	}

	/// Composites a premultiplied layer over the light.
	private void over(float red, float green, float blue, float alpha) {
		float beneath = 1f - alpha;
		this.lightRed = red + this.lightRed * beneath;
		this.lightGreen = green + this.lightGreen * beneath;
		this.lightBlue = blue + this.lightBlue * beneath;
		this.lightAlpha = alpha + this.lightAlpha * beneath;
	}

	@Override
	public boolean hd() {
		return this.hd;
	}

	public float band(Site site, Band band) {
		return this.bands[site.ordinal() * BANDS + band.ordinal()];
	}

	public int voiceCount() {
		return this.voices;
	}

	public Site voiceSite(int index) {
		return this.voiceSite[index];
	}

	public float voiceHz(int index) {
		return this.voiceHz[index];
	}

	public float voiceAmp(int index) {
		return this.voiceAmp[index];
	}

	public int clickCount() {
		return this.clicks;
	}

	public Site clickSite(int index) {
		return this.clickSite[index];
	}

	public float clickIntensity(int index) {
		return this.clickIntensity[index];
	}

	public float clickSharpness(int index) {
		return this.clickSharpness[index];
	}

	/// How much of the light any layer covers, 0 to 1. At 0, nothing is painted and the light
	/// shows its default color.
	public float lightAlpha() {
		return this.lightAlpha;
	}

	/// The light composited over black, in linear sRGB, 0 to 1.
	public float lightRed() {
		return this.lightRed;
	}

	public float lightGreen() {
		return this.lightGreen;
	}

	public float lightBlue() {
		return this.lightBlue;
	}

	public boolean isEmpty() {
		if (this.voices > 0 || this.clicks > 0 || this.lightAlpha > 0f) {
			return false;
		}
		for (float band : this.bands) {
			if (band > 0f) {
				return false;
			}
		}
		return true;
	}

	public void reset(boolean hd) {
		Arrays.fill(this.bands, 0f);
		this.voices = 0;
		this.clicks = 0;
		this.priority = 0;
		this.lightRed = this.lightGreen = this.lightBlue = this.lightAlpha = 0f;
		this.hd = hd;
	}

	/// Adds everything in `sample` to this frame, multiplied by `scale`, on top of everything
	/// mixed before it. Light is made more transparent by `scale` rather than darker.
	public void mix(MixedFrame sample, float scale) {
		this.priority++;
		if (scale <= 0f) {
			return;
		}
		if (sample.lightAlpha > 0f) {
			float alpha = clampAmp(sample.lightAlpha * scale);
			// keep the sample's color and change only how much it covers
			float k = alpha / sample.lightAlpha;
			this.over(sample.lightRed * k, sample.lightGreen * k, sample.lightBlue * k, alpha);
		}
		for (int i = 0; i < this.bands.length; i++) {
			this.bands[i] = Math.max(this.bands[i], clampAmp(sample.bands[i] * scale));
		}
		for (int i = 0; i < sample.voices; i++) {
			this.voice(sample.voiceSite[i], sample.voiceHz[i], sample.voiceAmp[i] * scale);
		}
		for (int i = 0; i < sample.clicks; i++) {
			this.click(sample.clickSite[i], sample.clickIntensity[i] * scale, sample.clickSharpness[i]);
		}
	}

	private static float clampAmp(float amp) {
		if (!(amp > 0f)) {
			return 0f; // also catches NaN
		}
		return Math.min(amp, 1f);
	}
}
