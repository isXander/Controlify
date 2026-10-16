/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.gyro;

/**
 * Implements rolling gyro calibration using exponential moving average
 * for background bias estimation during stationary periods (low angular velocity).
 * Bias application is handled by the input pipeline using the device settings.
 */
public class GyroCalibrator {
	// Exponential moving average (EMA) alpha value - lower = slower adaptation, more stable
	// 0.001 means it takes ~1000 samples to substantially update the bias estimate
	private static final float EMA_ALPHA = 0.001f;

	// Threshold for detecting stationary state (rad/s)
	// If all axes are below this threshold, we consider the gyro stationary
	private static final float STATIONARY_THRESHOLD = 0.02f;

	// Minimum number of consecutive stationary samples before updating bias
	private static final int MIN_STATIONARY_SAMPLES = 10;

	// Current bias estimate (offset to subtract from raw readings)
	private final GyroState biasEstimate = new GyroState(0, 0, 0);

	// Counter for consecutive stationary samples
	private int stationarySampleCount = 0;

	public void update(float x, float y, float z) {
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
			this.stationarySampleCount = 0;
		}
	}

	/**
	 * Check if the gyro appears to be stationary based on angular velocity.
	 */
	private boolean isStationary(float x, float y, float z) {
		return Math.abs(x) < STATIONARY_THRESHOLD
			&& Math.abs(y) < STATIONARY_THRESHOLD
			&& Math.abs(z) < STATIONARY_THRESHOLD;
	}

	/**
	 * Get the current bias estimate (for saving to config).
	 */
	public GyroStateC getCurrentBias() {
		return new GyroState(this.biasEstimate);
	}

	public void setBias(GyroStateC bias) {
		this.biasEstimate.set(bias.x(), bias.y(), bias.z());
		this.stationarySampleCount = 0;
	}

	public void reset() {
		this.biasEstimate.set(0, 0, 0);
		this.stationarySampleCount = 0;
	}
}
