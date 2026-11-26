/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import dev.isxander.controlify.api.ingame.look.LookOutput;
import dev.isxander.controlify.api.ingame.look.LookScales;

/// The look formula, free of Minecraft classes:
/// `total = RATE * dt * product(rate scales) + DIRECT * product(direct scales) + SCRIPTED`.
///
/// Contributions add and scales multiply, so the order of calls never changes the result.
public final class LookAccumulator {
	/// A rotation in degrees.
	public record Rotation(double yaw, double pitch) {
	}

	private double rateYaw, ratePitch;
	private double directYaw, directPitch;
	private double scriptedYaw, scriptedPitch;
	private double rateScaleYaw = 1, rateScalePitch = 1;
	private double directScaleYaw = 1, directScalePitch = 1;

	private final LookOutput output = new LookOutput() {
		@Override
		public void rate(double yawDegPerSec, double pitchDegPerSec) {
			rateYaw += yawDegPerSec;
			ratePitch += pitchDegPerSec;
		}

		@Override
		public void delta(double yawDeg, double pitchDeg) {
			directYaw += yawDeg;
			directPitch += pitchDeg;
		}

		@Override
		public void scripted(double yawDeg, double pitchDeg) {
			scriptedYaw += yawDeg;
			scriptedPitch += pitchDeg;
		}
	};

	private final LookScales scales = new LookScales() {
		@Override
		public void rate(double yaw, double pitch) {
			rateScaleYaw *= yaw;
			rateScalePitch *= pitch;
		}

		@Override
		public void direct(double yaw, double pitch) {
			directScaleYaw *= yaw;
			directScalePitch *= pitch;
		}
	};

	/// Where sources add their contributions.
	public LookOutput output() {
		return this.output;
	}

	/// Where scalers multiply.
	public LookScales scales() {
		return this.scales;
	}

	/// Starts a new frame, dropping everything from the last one.
	public void clear() {
		this.rateYaw = this.ratePitch = 0;
		this.directYaw = this.directPitch = 0;
		this.scriptedYaw = this.scriptedPitch = 0;
		this.rateScaleYaw = this.rateScalePitch = 1;
		this.directScaleYaw = this.directScalePitch = 1;
	}

	/// @param dt the frame time in seconds
	/// @return the rotation for the frame, in degrees
	public Rotation resolve(double dt) {
		return new Rotation(
				this.rateYaw * dt * this.rateScaleYaw + this.directYaw * this.directScaleYaw + this.scriptedYaw,
				this.ratePitch * dt * this.rateScalePitch + this.directPitch * this.directScalePitch + this.scriptedPitch
		);
	}
}
