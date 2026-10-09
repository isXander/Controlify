package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.input.SensorType;

/// Represents a single IMU sample
public record PhysicalSensorSignal(
		long timeNanos,
		SensorType sensorType,
		float x, float y, float z
) implements PhysicalSignal {
}
