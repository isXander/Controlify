/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

import org.jetbrains.annotations.ApiStatus;

/// Where a [LookSource] adds its contribution for the frame. Positive yaw turns right and
/// positive pitch looks down. Calls add to what other sources contributed.
@ApiStatus.NonExtendable
public interface LookOutput {
	/// Rate: a velocity in degrees per second. The framework multiplies by the frame time.
	void rate(double yawDegPerSec, double pitchDegPerSec);

	/// Direct: a physical displacement in degrees.
	void delta(double yawDeg, double pitchDeg);

	/// Scripted: a generated rotation in degrees that scalers cannot change.
	void scripted(double yawDeg, double pitchDeg);
}
