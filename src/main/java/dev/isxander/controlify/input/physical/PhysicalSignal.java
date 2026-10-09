package dev.isxander.controlify.input.physical;

public sealed interface PhysicalSignal permits PhysicalInputSignal, PhysicalSensorSignal, PhysicalSignal.Tick {
	long timeNanos();

	/// Emitted at the end of each polling / every frame.
	record Tick(long timeNanos, long deltaTimeNanos) implements PhysicalSignal {}
}
