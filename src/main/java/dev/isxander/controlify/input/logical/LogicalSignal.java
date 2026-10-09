package dev.isxander.controlify.input.logical;

import dev.isxander.controlify.input.SensorType;

public sealed interface LogicalSignal {
	/// The time this signal occurred.
	/// Every signal will have the same epoch.
	long timeNanos();

	/// Submitted once per frame.
	/// Allows bindings to accumulate state between ticks.
	record Tick(long timeNanos, long deltaTimeNanos) implements LogicalSignal {}

	sealed interface InputSignal extends LogicalSignal {
		LogicalInput input();
	}
	/// When the button is pressed down
	record ButtonDown(long timeNanos, LogicalInput input) implements InputSignal {}

	/// When the button is released
	record ButtonUp(long timeNanos, LogicalInput input) implements InputSignal {}

	/// When button is pressed and released in quick succession
	record Tapped(long timeNanos, LogicalInput input) implements InputSignal {}

	/// When two {@link Tapped taps} occur in quick succession
	record DoubleTapped(long timeNanos, LogicalInput input) implements InputSignal {}

	/// When a button is pressed and released, where no GUI navigation happened in between
	record GuiPress(long timeNanos, LogicalInput input) implements InputSignal {}

	/// When a button is held for a long period of time. Signaled whilst still held.
	record Held(long timeNanos, LogicalInput input) implements InputSignal {}

	/// When an axis value changes
	record AxisMoved(long timeNanos, LogicalInput input, float value, float delta) implements InputSignal {}

	/// Represents sensor data, which can be multi-dimensional.
	/// The data is expected to be tick-integrated deltas. They are not individual IMU samples.
	/// @see SensorAccumulatorStage
	record SensorInterval(long timeNanos, long deltaTimeNanos, SensorType sensorType, float x, float y, float z) implements LogicalSignal {}
}
