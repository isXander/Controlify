package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

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
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

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
