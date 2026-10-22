/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.signal;

import dev.isxander.controlify.impl.input.SensorType;

/// Represents a single IMU sample
/// @param timeNanos timestamp of the event in nanoseconds
/// @param sensorTimeNanos timestamp provided by the sensor firmware in nanoseconds.
///                        this may be out of sync with the system clock and therefore {@link timeNanos}
/// @param sensorType the type of sensor this signal is for
/// @param x the first component of the sensor data
/// @param y the second component of the sensor data;
///          may be zero if the sensor data is not at least 2-dimensional
/// @param z the third component of the sensor data;
///          may be zero if the sensor data is not at least 3-dimensional
public record PhysicalSensorSignal(
		long timeNanos,
		long sensorTimeNanos,
		SensorType sensorType,
		float x, float y, float z
) implements PhysicalSignal {
}
