package dev.isxander.controlify.input.signal;

public sealed interface PhysicalSignal permits PhysicalInputSignal, PhysicalSensorSignal, PhysicalSignal.Frame {
	/// timestamp of the signal in nanoseconds
	long timeNanos();

	/// Emitted at the end of each polling / every frame.
	record Frame(long timeNanos, long deltaTimeNanos) implements PhysicalSignal {}
}
