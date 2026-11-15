/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.haptic;

import dev.isxander.controlify.impl.config.settings.profile.HDHapticSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.driver.steam.SteamHapticReports;
import dev.isxander.controlify.impl.feedback.SteamHapticsRenderer;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

/// Present on the 2026 Steam Controller, whose haptics are not an audio stream but commands:
/// tones, ticks, clicks, pulse trains and sweeps, each a small HID output report that the
/// controller's firmware plays. The feedback engine's `SteamHapticsRenderer` drives this.
///
/// Rumble is not sent from here. It goes through the ordinary rumble component, which the
/// driver already turns into this controller's rumble report.
public class SteamHapticsComponent extends ControllerComponent implements SteamHapticsRenderer.Output {
	public static final Identifier ID = CUtil.rl("steam_haptics");

	/// Sends one complete report to the controller.
	@FunctionalInterface
	public interface ReportSender {
		/// @return whether the transport accepted the write; this is not a firmware acknowledgement
		boolean send(byte[] report);
	}

	private final ReportSender sender;
	private boolean failed;

	public SteamHapticsComponent(ReportSender sender) {
		super(ID);
		this.sender = sender;
	}

	/// A steady tone.
	///
	/// @param side       one of the `SteamHapticReports.Output.TRACKPAD_` or `INTERNAL_` targets
	/// @param hz         the frequency
	/// @param gainDb     loudness in decibels, 0 being full strength
	/// @param durationMs how long the tone plays if it is not sent again first
	public void tone(byte side, int hz, int gainDb, int durationMs) {
		this.send(SteamHapticReports.Output.lfoTone(side, gainDb, hz, durationMs, 0, 0));
	}

	/// A tone that pulses.
	///
	/// @param lfoHz    how many times a second it pulses
	/// @param lfoDepth how deep the pulsing is, as a percentage
	public void pulsingTone(byte side, int hz, int gainDb, int durationMs, int lfoHz, int lfoDepth) {
		this.send(SteamHapticReports.Output.lfoTone(side, gainDb, hz, durationMs, lfoHz, lfoDepth));
	}

	/// A crisp, short transient.
	public void tick(byte side, int gainDb) {
		this.send(SteamHapticReports.Output.command(side, SteamHapticReports.TYPE_TICK, gainDb));
	}

	/// A fuller transient, like a button press.
	public void click(byte side, int gainDb) {
		this.send(SteamHapticReports.Output.command(side, SteamHapticReports.TYPE_CLICK, gainDb));
	}

	/// A train of pulses. Trackpads use `Output.PULSE_TRACKPAD_` targets, whose left/right
	/// indices differ from those used by tones and clicks.
	public void pulse(byte side, int onMicros, int offMicros, int repeatCount) {
		this.send(SteamHapticReports.Output.pulse(side, onMicros, offMicros, repeatCount));
	}

	/// A sweep from one frequency to another.
	public void sweep(byte side, int gainDb, int durationMs, int startHz, int endHz) {
		this.send(SteamHapticReports.Output.logSweep(side, gainDb, durationMs, startHz, endHz));
	}

	/// Stops whatever is playing on a side.
	public void off(byte side) {
		this.send(SteamHapticReports.Output.command(side, SteamHapticReports.TYPE_OFF, 0));
	}

	private void send(byte[] report) {
		if (this.failed) {
			return;
		}
		if (!this.sender.send(report)) {
			// the controller or the bundled SDL does not take these reports; do not keep trying
			this.failed = true;
			CUtil.LOGGER.warn("Controller rejected a Steam haptic report; Steam haptics are disabled for it.");
		}
	}

	/// Whether reports are still being accepted.
	public boolean isWorking() {
		return !this.failed;
	}

	public HDHapticSettings settings() {
		return this.controller().settings().hdHaptic;
	}
}
