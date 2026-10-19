package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.action.Activator;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Produces a pulse when the input is tapped.
public record TapPulseActivator(LogicalInput boundInput) implements Activator {

	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.Tapped(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "Tap[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<TapPulseActivator> {
		public static final String TYPE_ID = "pulse_tap";
		public static final Config INSTANCE = new Config();

		@Override
		public TapPulseActivator create(LogicalInput boundInput) {
			return new TapPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<TapPulseActivator, Config> type() {
			return ActivatorConfigType.TAP_PULSE;
		}
	}
}
