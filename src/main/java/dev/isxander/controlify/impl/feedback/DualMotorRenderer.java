/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.Site;
import dev.isxander.controlify.impl.controller.rumble.RumbleState;

import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

/// Renders the body sites to the two rumble motors of an ordinary gamepad.
///
/// The motors cannot localise or pitch vibration, so `BODY`, `LEFT` and `RIGHT` all play
/// through both: the low band on the strong motor and the high band on the weak one. A
/// voice is folded into the band nearest its frequency, and a click becomes a short burst
/// on the weak motor.
public final class DualMotorRenderer implements FeedbackRenderer {
	/// Voices below this frequency play on the strong motor, the rest on the weak motor.
	static final float BAND_SPLIT_HZ = 100f;
	static final float CLICK_SECONDS = 0.03f;
	/// The state is resent at least this often, because drivers time rumble out.
	static final float RESEND_SECONDS = 1f;
	private static final float EPSILON = 1f / 512f;

	private static final Site[] BODY_SITES = { Site.BODY, Site.LEFT, Site.RIGHT };

	private final Consumer<RumbleState> output;
	private final BooleanSupplier nativeHdActive;
	private float sentLow, sentHigh;
	private float sinceSent;
	private float clickAmp, clickRemaining;

	/// @param output receives the state to rumble with, or [RumbleState#NONE] to stop
	public DualMotorRenderer(Consumer<RumbleState> output) {
		this(output, () -> false);
	}

	/// When native HD output is active, only bands are rendered here; voices and clicks
	/// belong to that renderer. This is checked each frame so fallback can change at runtime.
	public DualMotorRenderer(Consumer<RumbleState> output, BooleanSupplier nativeHdActive) {
		this.output = output;
		this.nativeHdActive = nativeHdActive;
	}

	@Override
	public void render(MixedFrame frame, float dt) {
		float low = 0f, high = 0f;
		for (Site site : BODY_SITES) {
			low = Math.max(low, frame.band(site, Band.LOW));
			high = Math.max(high, frame.band(site, Band.HIGH));
		}
		if (this.nativeHdActive.getAsBoolean()) {
			// Discard any fallback click tail when native haptics take over.
			this.clickRemaining = 0f;
			this.send(low, high, dt);
			return;
		}
		for (int i = 0; i < frame.voiceCount(); i++) {
			if (frame.voiceSite(i).isTrigger()) {
				continue;
			}
			if (frame.voiceHz(i) < BAND_SPLIT_HZ) {
				low = Math.max(low, frame.voiceAmp(i));
			} else {
				high = Math.max(high, frame.voiceAmp(i));
			}
		}

		for (int i = 0; i < frame.clickCount(); i++) {
			if (!frame.clickSite(i).isTrigger()) {
				this.clickAmp = Math.max(this.clickRemaining > 0f ? this.clickAmp : 0f, frame.clickIntensity(i));
				this.clickRemaining = CLICK_SECONDS;
			}
		}
		if (this.clickRemaining > 0f) {
			high = Math.max(high, this.clickAmp);
			this.clickRemaining -= dt;
		}

		this.send(low, high, dt);
	}

	@Override
	public void silence() {
		this.clickRemaining = 0f;
		this.send(0f, 0f, 0f);
	}

	private void send(float low, float high, float dt) {
		this.sinceSent += dt;
		boolean changed = Math.abs(low - this.sentLow) > EPSILON || Math.abs(high - this.sentHigh) > EPSILON
				// always let a stop through, however small the change
				|| (low == 0f && high == 0f && (this.sentLow != 0f || this.sentHigh != 0f));
		boolean active = low > 0f || high > 0f;
		if (!changed && !(active && this.sinceSent >= RESEND_SECONDS)) {
			return;
		}

		this.output.accept(active ? new RumbleState(low, high) : RumbleState.NONE);
		this.sentLow = low;
		this.sentHigh = high;
		this.sinceSent = 0f;
	}
}
