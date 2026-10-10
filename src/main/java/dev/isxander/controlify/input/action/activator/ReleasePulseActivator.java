package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

/// Produces a pulse when the input is released.
public record ReleasePulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.ButtonRelease(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "Release[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<ReleasePulseActivator> {
		public static final String TYPE_ID = "pulse_release";
		public static final Config INSTANCE = new Config();
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public ReleasePulseActivator create(LogicalInput boundInput) {
			return new ReleasePulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<ReleasePulseActivator, Config> type() {
			return ActivatorConfigType.RELEASE_PULSE;
		}
	}
}
