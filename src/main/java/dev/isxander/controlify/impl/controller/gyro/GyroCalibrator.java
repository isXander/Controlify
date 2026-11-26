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

	// Minimum time the gyro must have been stationary before updating bias, so a high sample rate
	// cannot satisfy the sample count within a few milliseconds.
	private static final long MIN_STATIONARY_NANOS = 100_000_000L;

	private long stationaryStartNanos;

	// Current bias estimate (offset to subtract from raw readings)
	private final Vector3f biasEstimate = new Vector3f(0, 0, 0);

	// Counter for consecutive stationary samples
	private int stationarySampleCount = 0;
	private final Vector3f windowMin = new Vector3f();
	private final Vector3f windowMax = new Vector3f();

	// Maximum accelerometer spread per axis (m/s^2) for a window to count as stationary.
	private static final float ACCEL_STEADY_THRESHOLD = 0.5f;
	private boolean hasAccelData;
	private final Vector3f latestAccel = new Vector3f();
	private boolean accelWindowValid;
	private final Vector3f accelWindowMin = new Vector3f();
	private final Vector3f accelWindowMax = new Vector3f();

	/**
	 * Feeds a raw accelerometer sample (m/s^2). Once any has been received, a gyro window
	 * only counts as stationary while the accelerometer is steady over the same window.
	 */
	public void updateAccel(float x, float y, float z) {
		if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
			return;
		}
		this.hasAccelData = true;
		this.latestAccel.set(x, y, z);
		if (!this.accelWindowValid) {
			this.accelWindowMin.set(x, y, z);
			this.accelWindowMax.set(x, y, z);
			this.accelWindowValid = true;
		} else {
			this.accelWindowMin.min(this.latestAccel);
			this.accelWindowMax.max(this.latestAccel);
		}
	}

	/**
	 * Restarts the accelerometer window alongside the gyro window, keeping the latest reading
	 * so ongoing accelerometer movement is not forgotten.
	 */
	private void resetAccelWindow() {
		this.accelWindowValid = this.hasAccelData;
		this.accelWindowMin.set(this.latestAccel);
		this.accelWindowMax.set(this.latestAccel);
	}

	private boolean isAccelSteady() {
		return !this.hasAccelData
				|| this.accelWindowMax.x - this.accelWindowMin.x < ACCEL_STEADY_THRESHOLD
				&& this.accelWindowMax.y - this.accelWindowMin.y < ACCEL_STEADY_THRESHOLD
				&& this.accelWindowMax.z - this.accelWindowMin.z < ACCEL_STEADY_THRESHOLD;
	}

	public void update(long timeNanos, float x, float y, float z) {
		if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
			this.stationarySampleCount = 0;
			return;
		}
		// Check if the gyro appears to be stationary
		boolean isStationary = isStationary(x, y, z);

		if (isStationary) {
			if (this.stationarySampleCount == 0) {
				this.stationaryStartNanos = timeNanos;
			}
			this.stationarySampleCount++;

			// Only update bias estimate if we've been stationary long enough
			if (this.stationarySampleCount >= MIN_STATIONARY_SAMPLES
					&& timeNanos - this.stationaryStartNanos >= MIN_STATIONARY_NANOS) {
				// Update bias estimate using exponential moving average
				// New bias = (1 - alpha) * old_bias + alpha * current_reading
				this.biasEstimate.x = (1 - EMA_ALPHA) * this.biasEstimate.x + EMA_ALPHA * x;
				this.biasEstimate.y = (1 - EMA_ALPHA) * this.biasEstimate.y + EMA_ALPHA * y;
				this.biasEstimate.z = (1 - EMA_ALPHA) * this.biasEstimate.z + EMA_ALPHA * z;
			}
		} else {
			// Reset stationary counter when motion is detected
			this.stationarySampleCount = 0;
			this.windowMin.set(x, y, z);
			this.windowMax.set(x, y, z);
			this.resetAccelWindow();
		}
	}

	/**
	 * Checks that the rate is near the current bias estimate and that the spread of raw samples is small.
	 * The spread alone is not enough: the accelerometer cannot see a smooth constant-rate rotation
	 * about the gravity axis (e.g. yaw while lying flat), which would otherwise be learned as bias.
	 */
	private boolean isStationary(float x, float y, float z) {
		if (Math.abs(x - this.biasEstimate.x) >= STATIONARY_THRESHOLD
				|| Math.abs(y - this.biasEstimate.y) >= STATIONARY_THRESHOLD
				|| Math.abs(z - this.biasEstimate.z) >= STATIONARY_THRESHOLD) {
			return false;
		}
		if (this.stationarySampleCount == 0) {
			this.windowMin.set(x, y, z);
			this.windowMax.set(x, y, z);
			this.resetAccelWindow();
		}
		this.windowMin.min(new Vector3f(x, y, z));
		this.windowMax.max(new Vector3f(x, y, z));
		return this.isAccelSteady()
				&& this.windowMax.x - this.windowMin.x < STATIONARY_THRESHOLD
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
		this.hasAccelData = false;
		this.accelWindowValid = false;
	}
}
