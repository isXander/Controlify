package dev.isxander.controlify.input.physical;

import net.minecraft.resources.Identifier;

public sealed interface PhysicalInputSignal extends PhysicalSignal {
	/// @return the identifier of the physical input
	Identifier input();

	/// Represents a button press/release event.
	/// @param timeNanos the timestamp of the event in nanoseconds
	/// @param input the identifier of the physical button
	/// @param state the state of the button, where true indicates pressed and false indicates released
	record ButtonChanged(long timeNanos, Identifier input, boolean state) implements PhysicalInputSignal {}

	/// Represents an axis movement event.
	/// @param timeNanos the timestamp of the event in nanoseconds
	/// @param input the identifier of the physical axis
	/// @param value the position of the axis, *always* in the range `[0.0, 1.0]`
	record AxisMoved(long timeNanos, Identifier input, float value) implements PhysicalInputSignal {}
}
