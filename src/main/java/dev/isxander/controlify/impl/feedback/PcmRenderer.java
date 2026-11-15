/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.Site;

import java.util.Arrays;

/// Renders the body sites as synthesised audio for controllers whose haptic actuators are
/// driven by a PCM stream, such as the DualSense.
///
/// Nothing here is a sound file. Each band and voice is a sine oscillator whose amplitude
/// follows the mixed frame, and each click is a short decaying burst. Every frame, enough
/// audio is generated to keep [#LEAD_SECONDS] queued in the sink.
///
/// `LEFT` and `RIGHT` play on their own actuator and `BODY` on both. Trigger sites are
/// not rendered here.
public final class PcmRenderer implements FeedbackRenderer {
	/// The tones the two rumble bands are played as. Chosen by ear; tune by feel.
	static final float LOW_BAND_HZ = 60f;
	static final float HIGH_BAND_HZ = 180f;

	/// How much audio is kept queued ahead of playback. Less is lower latency, but risks
	/// gaps when a frame runs long.
	static final float LEAD_SECONDS = 0.04f;
	/// The most audio generated in one frame, so a long stall is not filled with a stale tone.
	static final float MAX_BLOCK_SECONDS = 0.1f;
	/// How long the fade to silence is when output is switched off.
	static final float FADE_SECONDS = 0.01f;
	static final float MASTER_GAIN = 0.9f;

	private static final int MAX_OSCILLATORS = 16;
	private static final int MAX_BURSTS = 8;
	private static final Site[] BODY_SITES = { Site.BODY, Site.LEFT, Site.RIGHT };

	private final PcmSink sink;
	private final float sampleRate;
	private float[] buffer = new float[0];

	// oscillators: one per (site, frequency) that is sounding or fading out
	private final boolean[] oscActive = new boolean[MAX_OSCILLATORS];
	private final Site[] oscSite = new Site[MAX_OSCILLATORS];
	private final float[] oscHz = new float[MAX_OSCILLATORS];
	private final double[] oscPhase = new double[MAX_OSCILLATORS];
	private final float[] oscAmp = new float[MAX_OSCILLATORS];
	private final float[] oscTarget = new float[MAX_OSCILLATORS];

	// bursts: clicks that are still ringing
	private final boolean[] burstActive = new boolean[MAX_BURSTS];
	private final Site[] burstSite = new Site[MAX_BURSTS];
	private final float[] burstHz = new float[MAX_BURSTS];
	private final float[] burstDecay = new float[MAX_BURSTS];
	private final float[] burstAmp = new float[MAX_BURSTS];
	private final float[] burstTime = new float[MAX_BURSTS];

	public PcmRenderer(PcmSink sink) {
		this.sink = sink;
		this.sampleRate = sink.sampleRate();
	}

	@Override
	public void render(MixedFrame frame, float dt) {
		Arrays.fill(this.oscTarget, 0f);

		for (Site site : BODY_SITES) {
			this.drive(site, LOW_BAND_HZ, frame.band(site, Band.LOW));
			this.drive(site, HIGH_BAND_HZ, frame.band(site, Band.HIGH));
		}
		for (int i = 0; i < frame.voiceCount(); i++) {
			if (!frame.voiceSite(i).isTrigger()) {
				this.drive(frame.voiceSite(i), frame.voiceHz(i), frame.voiceAmp(i));
			}
		}
		for (int i = 0; i < frame.clickCount(); i++) {
			if (!frame.clickSite(i).isTrigger()) {
				this.strike(frame.clickSite(i), frame.clickIntensity(i), frame.clickSharpness(i));
			}
		}

		if (!this.isSounding()) {
			return;
		}
		int wanted = Math.round(LEAD_SECONDS * this.sampleRate) - this.sink.queuedFrames();
		this.generate(Math.min(wanted, Math.round(MAX_BLOCK_SECONDS * this.sampleRate)));
	}

	@Override
	public void silence() {
		Arrays.fill(this.oscTarget, 0f);
		Arrays.fill(this.burstActive, false);
		if (this.isSounding()) {
			// fade out rather than cut, which would be felt as a click
			this.generate(Math.round(FADE_SECONDS * this.sampleRate));
		}
	}

	/// Sets the amplitude an oscillator should reach by the end of this frame's audio.
	private void drive(Site site, float hz, float amp) {
		if (amp <= 0f) {
			return;
		}
		int free = -1;
		for (int i = 0; i < MAX_OSCILLATORS; i++) {
			if (!this.oscActive[i]) {
				if (free < 0) {
					free = i;
				}
			} else if (this.oscSite[i] == site && Math.abs(this.oscHz[i] - hz) < 0.5f) {
				this.oscTarget[i] = Math.max(this.oscTarget[i], amp);
				return;
			}
		}
		if (free < 0) {
			return; // every oscillator is busy; this tone is dropped
		}
		this.oscActive[free] = true;
		this.oscSite[free] = site;
		this.oscHz[free] = hz;
		this.oscPhase[free] = 0d;
		this.oscAmp[free] = 0f;
		this.oscTarget[free] = amp;
	}

	private void strike(Site site, float intensity, float sharpness) {
		for (int i = 0; i < MAX_BURSTS; i++) {
			if (!this.burstActive[i]) {
				this.burstActive[i] = true;
				this.burstSite[i] = site;
				// a dull thud is low and rings on; a crisp tick is high and short
				this.burstHz[i] = lerp(sharpness, 50f, 250f);
				this.burstDecay[i] = lerp(sharpness, 0.03f, 0.006f);
				this.burstAmp[i] = intensity;
				this.burstTime[i] = 0f;
				return;
			}
		}
	}

	private boolean isSounding() {
		for (int i = 0; i < MAX_OSCILLATORS; i++) {
			if (this.oscActive[i]) {
				return true;
			}
		}
		for (int i = 0; i < MAX_BURSTS; i++) {
			if (this.burstActive[i]) {
				return true;
			}
		}
		return false;
	}

	private void generate(int frames) {
		if (frames <= 0) {
			return;
		}
		if (this.buffer.length < frames * 2) {
			this.buffer = new float[frames * 2];
		}
		java.util.Arrays.fill(this.buffer, 0, frames * 2, 0f);

		float secondsPerFrame = 1f / this.sampleRate;

		for (int osc = 0; osc < MAX_OSCILLATORS; osc++) {
			if (!this.oscActive[osc]) {
				continue;
			}
			float left = this.oscSite[osc] == Site.RIGHT ? 0f : 1f;
			float right = this.oscSite[osc] == Site.LEFT ? 0f : 1f;
			float from = this.oscAmp[osc];
			float to = this.oscTarget[osc];
			double phase = this.oscPhase[osc];
			double step = 2d * Math.PI * this.oscHz[osc] / this.sampleRate;

			for (int i = 0; i < frames; i++) {
				// ramp the amplitude across the block so level changes never click
				float amp = from + (to - from) * (i + 1) / frames;
				float sample = amp * (float) Math.sin(phase);
				phase += step;
				this.buffer[i * 2] += sample * left;
				this.buffer[i * 2 + 1] += sample * right;
			}

			this.oscPhase[osc] = phase % (2d * Math.PI);
			this.oscAmp[osc] = to;
			if (to <= 0f) {
				this.oscActive[osc] = false;
			}
		}

		for (int burst = 0; burst < MAX_BURSTS; burst++) {
			if (!this.burstActive[burst]) {
				continue;
			}
			float left = this.burstSite[burst] == Site.RIGHT ? 0f : 1f;
			float right = this.burstSite[burst] == Site.LEFT ? 0f : 1f;
			float time = this.burstTime[burst];
			float decay = this.burstDecay[burst];

			for (int i = 0; i < frames; i++) {
				float envelope = this.burstAmp[burst] * (float) Math.exp(-time / decay);
				float sample = envelope * (float) Math.sin(2d * Math.PI * this.burstHz[burst] * time);
				time += secondsPerFrame;
				this.buffer[i * 2] += sample * left;
				this.buffer[i * 2 + 1] += sample * right;
			}

			this.burstTime[burst] = time;
			if (time > decay * 6f) {
				this.burstActive[burst] = false; // decayed to under 0.3% of its strength
			}
		}

		for (int i = 0; i < frames * 2; i++) {
			// tanh keeps a loud mix of many tones from clipping harshly
			this.buffer[i] = (float) Math.tanh(this.buffer[i] * MASTER_GAIN);
		}

		this.sink.write(this.buffer, frames);
	}

	private static float lerp(float t, float from, float to) {
		return from + (to - from) * Math.clamp(t, 0f, 1f);
	}
}
