/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.Site;
import dev.isxander.controlify.impl.controller.rumble.TriggerRumbleState;

import java.util.function.Consumer;

/// Renders `LEFT_TRIGGER` and `RIGHT_TRIGGER` to the motors inside the triggers.
///
/// Each trigger has a single motor, so its amplitude is the largest of both bands and any
/// voice at that site; frequency is ignored. A click becomes a short burst.
public final class TriggerMotorRenderer implements FeedbackRenderer {
	private static final float EPSILON = 1f / 512f;

	private final Consumer<TriggerRumbleState> output;
	private float sentLeft, sentRight;
	private float sinceSent;
	private final float[] clickAmp = new float[2];
	private final float[] clickRemaining = new float[2];

	/// @param output receives the state to rumble with, or [TriggerRumbleState#NONE] to stop
	public TriggerMotorRenderer(Consumer<TriggerRumbleState> output) {
		this.output = output;
	}

	@Override
	public void render(MixedFrame frame, float dt) {
		float left = Math.max(frame.band(Site.LEFT_TRIGGER, Band.LOW), frame.band(Site.LEFT_TRIGGER, Band.HIGH));
		float right = Math.max(frame.band(Site.RIGHT_TRIGGER, Band.LOW), frame.band(Site.RIGHT_TRIGGER, Band.HIGH));
		for (int i = 0; i < frame.voiceCount(); i++) {
			if (frame.voiceSite(i) == Site.LEFT_TRIGGER) {
				left = Math.max(left, frame.voiceAmp(i));
			} else if (frame.voiceSite(i) == Site.RIGHT_TRIGGER) {
				right = Math.max(right, frame.voiceAmp(i));
			}
		}

		for (int i = 0; i < frame.clickCount(); i++) {
			if (frame.clickSite(i).isTrigger()) {
				int side = frame.clickSite(i) == Site.LEFT_TRIGGER ? 0 : 1;
				this.clickAmp[side] = Math.max(this.clickRemaining[side] > 0f ? this.clickAmp[side] : 0f, frame.clickIntensity(i));
				this.clickRemaining[side] = DualMotorRenderer.CLICK_SECONDS;
			}
		}
		if (this.clickRemaining[0] > 0f) {
			left = Math.max(left, this.clickAmp[0]);
			this.clickRemaining[0] -= dt;
		}
		if (this.clickRemaining[1] > 0f) {
			right = Math.max(right, this.clickAmp[1]);
			this.clickRemaining[1] -= dt;
		}

		this.send(left, right, dt);
	}

	@Override
	public void silence() {
		this.clickRemaining[0] = this.clickRemaining[1] = 0f;
		this.send(0f, 0f, 0f);
	}

	private void send(float left, float right, float dt) {
		this.sinceSent += dt;
		boolean changed = Math.abs(left - this.sentLeft) > EPSILON || Math.abs(right - this.sentRight) > EPSILON
				|| (left == 0f && right == 0f && (this.sentLeft != 0f || this.sentRight != 0f));
		boolean active = left > 0f || right > 0f;
		if (!changed && !(active && this.sinceSent >= DualMotorRenderer.RESEND_SECONDS)) {
			return;
		}

		this.output.accept(active ? new TriggerRumbleState(left, right) : TriggerRumbleState.NONE);
		this.sentLeft = left;
		this.sentRight = right;
		this.sinceSent = 0f;
	}
}
