package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

public record ContinuousActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.AxisMoved s && s.input().equals(this.boundInput)) {
			acc.setContinuous(s.value());
		}
	}

	@Override
	public String describe() {
		return "Continuous[" + boundInput().toString() + "]";
	}

	public record Config() implements ActivatorConfig<ContinuousActivator> {
		public static final String TYPE_ID = "continuous";
		public static final Config INSTANCE = new Config();
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public ContinuousActivator create(LogicalInput boundInput) {
			return new ContinuousActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<ContinuousActivator, Config> type() {
			return ActivatorConfigType.CONTINUOUS;
		}
	}
}
