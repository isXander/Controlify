package dev.isxander.controlify.action.activator;

import dev.isxander.controlify.action.Accumulator;
import dev.isxander.controlify.action.Activator;
import dev.isxander.controlify.input.LogicalInput;
import dev.isxander.controlify.input.signal.LogicalSignal;

public class NoopActivator implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {

	}

	@Override
	public LogicalInput boundInput() {
		return null;
	}

	@Override
	public String describe() {
		return "Noop";
	}
}
