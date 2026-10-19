package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.action.Activator;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

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
