package dev.isxander.controlify.action.activator;

import dev.isxander.controlify.action.Accumulator;
import dev.isxander.controlify.action.Activator;
import dev.isxander.controlify.input.LogicalInput;
import dev.isxander.controlify.input.signal.LogicalSignal;

/// Produces a pulse when the input is pressed.
public record PressPulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.ButtonPress(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "Press[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<PressPulseActivator> {
		public static final String TYPE_ID = "pulse_press";
		public static final Config INSTANCE = new Config();

		@Override
		public PressPulseActivator create(LogicalInput boundInput) {
			return new PressPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<PressPulseActivator, Config> type() {
			return ActivatorConfigType.PRESS_PULSE;
		}
	}
}
