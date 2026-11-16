/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.controller.gyro.GyroSpace;

import java.util.Objects;

/// Tuning for {@link MotionStage}. Rates are in degrees per second.
///
/// @param space how controller rotation maps to yaw and pitch
/// @param sensitivity real-world ratio; 1 means one degree of controller rotation gives one degree of output
/// @param verticalRatio extra multiplier applied to pitch only
/// @param fastSensitivity sensitivity used at and above {@code accelFastThreshold}
/// @param accelSlowThreshold rate at and below which {@code sensitivity} applies
/// @param accelFastThreshold rate at and above which {@code fastSensitivity} applies;
///                           acceleration is off when this does not exceed the slow threshold
/// @param smoothThreshold rate below which motion is fully smoothed; 0 disables smoothing
/// @param smoothTime moving average window in seconds
/// @param tightenThreshold rates below this are scaled down proportionally; 0 disables
public record MotionParams(
		GyroSpace space,
		float sensitivity,
		float verticalRatio,
		float fastSensitivity,
		float accelSlowThreshold,
		float accelFastThreshold,
		float smoothThreshold,
		float smoothTime,
		float tightenThreshold
) {
	public static final MotionParams DEFAULT = new MotionParams(
			GyroSpace.LOCAL_YAW, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f
	);

	public MotionParams {
		Objects.requireNonNull(space);
	}

	public MotionParams withSpace(GyroSpace space) {
		return new MotionParams(space, sensitivity, verticalRatio, fastSensitivity,
				accelSlowThreshold, accelFastThreshold, smoothThreshold, smoothTime, tightenThreshold);
	}

	public MotionParams withSensitivity(float sensitivity) {
		return new MotionParams(space, sensitivity, verticalRatio, fastSensitivity,
				accelSlowThreshold, accelFastThreshold, smoothThreshold, smoothTime, tightenThreshold);
	}

	public MotionParams withVerticalRatio(float verticalRatio) {
		return new MotionParams(space, sensitivity, verticalRatio, fastSensitivity,
				accelSlowThreshold, accelFastThreshold, smoothThreshold, smoothTime, tightenThreshold);
	}

	public MotionParams withAcceleration(float fastSensitivity, float slowThreshold, float fastThreshold) {
		return new MotionParams(space, sensitivity, verticalRatio, fastSensitivity,
				slowThreshold, fastThreshold, smoothThreshold, smoothTime, tightenThreshold);
	}

	public MotionParams withSmoothing(float threshold, float time) {
		return new MotionParams(space, sensitivity, verticalRatio, fastSensitivity,
				accelSlowThreshold, accelFastThreshold, threshold, time, tightenThreshold);
	}

	public MotionParams withTightening(float threshold) {
		return new MotionParams(space, sensitivity, verticalRatio, fastSensitivity,
				accelSlowThreshold, accelFastThreshold, smoothThreshold, smoothTime, threshold);
	}
}
