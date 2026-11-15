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
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/// A set of bands, voices and clicks. Used both as the [FeedbackFrame] one effect writes a
/// sample into, and as the mix of every playback that renderers read.
///
/// Bands mix by maximum. Voices at the same site and frequency merge by maximum; when there
/// are more than [#MAX_VOICES], the quietest are dropped. Clicks are kept individually, up to
/// [#MAX_CLICKS] per frame.
public final class MixedFrame implements FeedbackFrame {
	public static final int MAX_VOICES = 8;
	public static final int MAX_CLICKS = 8;

	private static final Site[] SITES = Site.values();
	private static final int BANDS = Band.values().length;

	private final float[] bands = new float[SITES.length * BANDS];

	private final Site[] voiceSite = new Site[MAX_VOICES];
	private final float[] voiceHz = new float[MAX_VOICES];
	private final float[] voiceAmp = new float[MAX_VOICES];
	private int voices;

	private final Site[] clickSite = new Site[MAX_CLICKS];
	private final float[] clickIntensity = new float[MAX_CLICKS];
	private final float[] clickSharpness = new float[MAX_CLICKS];
	private int clicks;

	private boolean hd;

	// ---- written by effects ----

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
				return;
			}
		}
		int slot = this.voices;
		if (slot == MAX_VOICES) {
			// full: replace the quietest voice if this one is louder
			slot = 0;
			for (int i = 1; i < MAX_VOICES; i++) {
				if (this.voiceAmp[i] < this.voiceAmp[slot]) {
					slot = i;
				}
			}
			if (this.voiceAmp[slot] >= amp) {
				return;
			}
		} else {
			this.voices++;
		}
		this.voiceSite[slot] = site;
		this.voiceHz[slot] = hz;
		this.voiceAmp[slot] = amp;
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
	public boolean hd() {
		return this.hd;
	}

	// ---- read by renderers ----

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

	public boolean isEmpty() {
		if (this.voices > 0 || this.clicks > 0) {
			return false;
		}
		for (float band : this.bands) {
			if (band > 0f) {
				return false;
			}
		}
		return true;
	}

	// ---- used by the engine ----

	public void reset(boolean hd) {
		Arrays.fill(this.bands, 0f);
		this.voices = 0;
		this.clicks = 0;
		this.hd = hd;
	}

	/// Adds everything in `sample` to this frame, multiplied by `scale`.
	public void mix(MixedFrame sample, float scale) {
		if (scale <= 0f) {
			return;
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
