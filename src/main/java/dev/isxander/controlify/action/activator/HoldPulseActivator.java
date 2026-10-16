package dev.isxander.controlify.action.activator;

import dev.isxander.controlify.action.Accumulator;
import dev.isxander.controlify.action.Activator;
import dev.isxander.controlify.input.LogicalInput;
import dev.isxander.controlify.input.signal.LogicalSignal;

/// Produces a pulse once the input reaches the hold duration.
public record HoldPulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.Held(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "Hold[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<HoldPulseActivator> {
		public static final String TYPE_ID = "pulse_hold";
		public static final Config INSTANCE = new Config();

		@Override
		public HoldPulseActivator create(LogicalInput boundInput) {
			return new HoldPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<HoldPulseActivator, Config> type() {
			return ActivatorConfigType.HOLD_PULSE;
		}
	}
}
