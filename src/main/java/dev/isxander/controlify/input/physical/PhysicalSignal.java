package dev.isxander.controlify.input.physical;

public sealed interface PhysicalSignal permits PhysicalInputSignal, PhysicalSensorSignal, PhysicalSignal.Frame {
	long timeNanos();

	/// Emitted at the end of each polling / every frame.
	record Frame(long timeNanos, long deltaTimeNanos) implements PhysicalSignal {}
}
