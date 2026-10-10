package dev.isxander.controlify.input.action.activator;

import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

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
