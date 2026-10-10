package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

/// Produces a pulse when the input is double tapped.
public record DoubleTapPulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.DoubleTapped(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "DoubleTap[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<DoubleTapPulseActivator> {
		public static final String TYPE_ID = "pulse_double_tap";
		public static final Config INSTANCE = new Config();
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public DoubleTapPulseActivator create(LogicalInput boundInput) {
			return new DoubleTapPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<DoubleTapPulseActivator, Config> type() {
			return ActivatorConfigType.DOUBLE_TAP_PULSE;
		}
	}
}
