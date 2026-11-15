/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Site;
import dev.isxander.controlify.impl.driver.steam.SteamHapticReports;

/// Renders voices and clicks as Steam Controller haptic commands.
///
/// The controller plays tones itself, so a voice becomes a "play this frequency at this
/// gain" command rather than audio. Each command carries a short duration and is sent again
/// while the voice lasts, so a tone ends by itself shortly after its voice does. Each side
/// plays one tone at a time: the strongest voice at that side.
///
/// Bands are not rendered here. They are rumble, which reaches this controller through the
/// ordinary `DualMotorRenderer`. Trigger sites are ignored.
public final class SteamHapticsRenderer implements FeedbackRenderer {
	/// What this renderer sends. Implemented by `SteamHapticsComponent`.
	public interface Output {
		void tone(byte side, int hz, int gainDb, int durationMs);

		void tick(byte side, int gainDb);

		void click(byte side, int gainDb);
	}

	/// How long each tone command plays for if it is not renewed.
	static final int TONE_DURATION_MS = 80;
	/// A sounding tone is renewed this often, comfortably inside [#TONE_DURATION_MS].
	static final float RESEND_SECONDS = 0.04f;
	/// The quietest gain sent; anything quieter is treated as silence.
	static final int MIN_GAIN_DB = -40;
	/// Clicks at least this sharp are sent as a tick, duller ones as a click.
	static final float TICK_SHARPNESS = 0.5f;

	private static final int LEFT = 0, RIGHT = 1;

	private final Output output;
	private final int[] sentHz = new int[2];
	private final int[] sentGain = new int[2];
	private final float[] sinceSent = new float[2];

	public SteamHapticsRenderer(Output output) {
		this.output = output;
		this.sentGain[LEFT] = this.sentGain[RIGHT] = Integer.MIN_VALUE;
	}

	@Override
	public void render(MixedFrame frame, float dt) {
		// the strongest voice each side should play; a BODY voice competes on both
		float[] amp = new float[2];
		int[] hz = new int[2];
		for (int i = 0; i < frame.voiceCount(); i++) {
			Site site = frame.voiceSite(i);
			if (site.isTrigger()) {
				continue;
			}
			for (int side = LEFT; side <= RIGHT; side++) {
				boolean plays = site == Site.BODY || (site == Site.LEFT) == (side == LEFT);
				if (plays && frame.voiceAmp(i) > amp[side]) {
					amp[side] = frame.voiceAmp(i);
					hz[side] = Math.round(frame.voiceHz(i));
				}
			}
		}

		int[] gain = { gainDb(amp[LEFT]), gainDb(amp[RIGHT]) };
		boolean[] due = new boolean[2];
		for (int side = LEFT; side <= RIGHT; side++) {
			this.sinceSent[side] += dt;
			boolean sounding = gain[side] > MIN_GAIN_DB;
			boolean changed = hz[side] != this.sentHz[side] || gain[side] != this.sentGain[side];
			due[side] = sounding && (changed || this.sinceSent[side] >= RESEND_SECONDS);
			if (!sounding) {
				// forget what was sent, so the same tone starting again counts as a change
				this.sentGain[side] = Integer.MIN_VALUE;
			}
		}

		if (due[LEFT] && due[RIGHT] && hz[LEFT] == hz[RIGHT] && gain[LEFT] == gain[RIGHT]) {
			// the same tone on both sides is one command
			this.output.tone(SteamHapticReports.Output.TRACKPAD_BOTH, hz[LEFT], gain[LEFT], TONE_DURATION_MS);
			this.sent(LEFT, hz[LEFT], gain[LEFT]);
			this.sent(RIGHT, hz[RIGHT], gain[RIGHT]);
		} else {
			if (due[LEFT]) {
				this.output.tone(SteamHapticReports.Output.TRACKPAD_LEFT, hz[LEFT], gain[LEFT], TONE_DURATION_MS);
				this.sent(LEFT, hz[LEFT], gain[LEFT]);
			}
			if (due[RIGHT]) {
				this.output.tone(SteamHapticReports.Output.TRACKPAD_RIGHT, hz[RIGHT], gain[RIGHT], TONE_DURATION_MS);
				this.sent(RIGHT, hz[RIGHT], gain[RIGHT]);
			}
		}

		for (int i = 0; i < frame.clickCount(); i++) {
			Site site = frame.clickSite(i);
			if (site.isTrigger()) {
				continue;
			}
			int clickGain = gainDb(frame.clickIntensity(i));
			if (clickGain <= MIN_GAIN_DB) {
				continue;
			}
			byte side = switch (site) {
				case LEFT -> SteamHapticReports.Output.TRACKPAD_LEFT;
				case RIGHT -> SteamHapticReports.Output.TRACKPAD_RIGHT;
				default -> SteamHapticReports.Output.TRACKPAD_BOTH;
			};
			if (frame.clickSharpness(i) >= TICK_SHARPNESS) {
				this.output.tick(side, clickGain);
			} else {
				this.output.click(side, clickGain);
			}
		}
	}

	@Override
	public void silence() {
		// nothing to send: tones are short and simply stop being renewed
		this.sentGain[LEFT] = this.sentGain[RIGHT] = Integer.MIN_VALUE;
	}

	private void sent(int side, int hz, int gain) {
		this.sentHz[side] = hz;
		this.sentGain[side] = gain;
		this.sinceSent[side] = 0f;
	}

	/// Converts a 0 to 1 amplitude to decibels, where 1 is 0 dB and each halving is about -6 dB.
	static int gainDb(float amp) {
		if (amp <= 0f) {
			return MIN_GAIN_DB;
		}
		return Math.max(MIN_GAIN_DB, Math.round(20f * (float) Math.log10(Math.min(amp, 1f))));
	}
}
