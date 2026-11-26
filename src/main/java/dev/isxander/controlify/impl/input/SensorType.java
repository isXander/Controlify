/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input;

public enum SensorType {
	/// Sensor components are `xyz` and in radians.
	GYROSCOPE,
	/// Sensor components are `xyz` and in radians.
	GYROSCOPE_L,
	/// Sensor components are `xyz` and in radians.
	GYROSCOPE_R,
	/// Sensor components are `xyz` and in m/s^2.
	ACCELEROMETER,
	/// Sensor components are `xyz` and in m/s^2.
	ACCELEROMETER_L,
	/// Sensor components are `xyz` and in m/s^2.
	ACCELEROMETER_R;

	public boolean isGyroscope() {
		return this == GYROSCOPE || this == GYROSCOPE_L || this == GYROSCOPE_R;
	}
	public boolean isAccelerometer() {
		return this == ACCELEROMETER || this == ACCELEROMETER_L || this == ACCELEROMETER_R;
	}
}
