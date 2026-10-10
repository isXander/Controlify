package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

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
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

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
