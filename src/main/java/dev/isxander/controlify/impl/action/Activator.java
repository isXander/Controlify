package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Processes signals and activates an {@link Action}.
public interface Activator {

	/// Accepts a {@link LogicalSignal} and potentially
	/// submits state into the action accumulator.
	void onSignal(LogicalSignal signal, Accumulator acc);

	/// The logical input this is bound to.
	LogicalInput boundInput();

	/// gives a string representation of this activator
	String describe();

}
