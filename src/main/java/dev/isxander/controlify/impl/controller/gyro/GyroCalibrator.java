/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.gyro;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Implements rolling gyro calibration using exponential moving average
 * for background bias estimation during periods of stable angular velocity.
 * Bias application is handled by the input pipeline using the device settings.
 */
public class GyroCalibrator {
	// Exponential moving average (EMA) alpha value - lower = slower adaptation, more stable
	// 0.001 means it takes ~1000 samples to substantially update the bias estimate
	private static final float EMA_ALPHA = 0.001f;

	// Maximum sample spread during a stationary window (rad/s), independent of zero-rate bias.
	private static final float STATIONARY_THRESHOLD = 0.02f;

	// Minimum number of consecutive stationary samples before updating bias
	private static final int MIN_STATIONARY_SAMPLES = 10;

	// Current bias estimate (offset to subtract from raw readings)
	private final Vector3f biasEstimate = new Vector3f(0, 0, 0);

	// Counter for consecutive stationary samples
	private int stationarySampleCount = 0;
	private final Vector3f windowMin = new Vector3f();
	private final Vector3f windowMax = new Vector3f();

	public void update(float x, float y, float z) {
		if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
			this.stationarySampleCount = 0;
			return;
		}
		// Check if the gyro appears to be stationary
		boolean isStationary = isStationary(x, y, z);

		if (isStationary) {
			this.stationarySampleCount++;

			// Only update bias estimate if we've been stationary long enough
			if (this.stationarySampleCount >= MIN_STATIONARY_SAMPLES) {
				// Update bias estimate using exponential moving average
				// New bias = (1 - alpha) * old_bias + alpha * current_reading
				this.biasEstimate.x = (1 - EMA_ALPHA) * this.biasEstimate.x + EMA_ALPHA * x;
				this.biasEstimate.y = (1 - EMA_ALPHA) * this.biasEstimate.y + EMA_ALPHA * y;
				this.biasEstimate.z = (1 - EMA_ALPHA) * this.biasEstimate.z + EMA_ALPHA * z;
			}
		} else {
			// Reset stationary counter when motion is detected
			this.stationarySampleCount = 1;
			this.windowMin.set(x, y, z);
			this.windowMax.set(x, y, z);
		}
	}

	/**
	 * Check the spread of raw samples, so hardware bias does not prevent calibration.
	 */
	private boolean isStationary(float x, float y, float z) {
		if (this.stationarySampleCount == 0) {
			this.windowMin.set(x, y, z);
			this.windowMax.set(x, y, z);
		}
		this.windowMin.min(new Vector3f(x, y, z));
		this.windowMax.max(new Vector3f(x, y, z));
		return this.windowMax.x - this.windowMin.x < STATIONARY_THRESHOLD
				&& this.windowMax.y - this.windowMin.y < STATIONARY_THRESHOLD
				&& this.windowMax.z - this.windowMin.z < STATIONARY_THRESHOLD;
	}

	/**
	 * Get the current bias estimate (for saving to config).
	 */
	public Vector3f getCurrentBias() {
		return new Vector3f(this.biasEstimate);
	}

	public void setBias(Vector3fc bias) {
		this.biasEstimate.set(bias.x(), bias.y(), bias.z());
		this.stationarySampleCount = 0;
	}

	public void reset() {
		this.biasEstimate.set(0, 0, 0);
		this.stationarySampleCount = 0;
	}
}
