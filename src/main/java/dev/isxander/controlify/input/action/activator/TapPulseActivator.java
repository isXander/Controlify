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

	public record TapPulseActivatorConfig() implements ActivatorFactory<TapPulseActivator> {
		public static final String TYPE_ID = "pulse_tap";
		public static final TapPulseActivatorConfig INSTANCE = new TapPulseActivatorConfig();
		public static final MapCodec<TapPulseActivatorConfig> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public TapPulseActivator create(LogicalInput boundInput) {
			return new TapPulseActivator(boundInput);
		}
	}
}
