package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

/// Keeps the latch active from button press until release, without a hold delay.
public record HoldLatchActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		switch (signal) {
			case LogicalSignal.ButtonPress(_, LogicalInput input) -> {
				if (this.boundInput.equals(input)) {
					acc.setLatch(true);
				}
			}
			case LogicalSignal.ButtonRelease(_, LogicalInput input) -> {
				if (this.boundInput.equals(input)) {
					acc.setLatch(false);
				}
			}
			default -> {}
		}
	}

	@Override
	public String describe() {
		return "HoldLatch[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<HoldLatchActivator> {
		public static final String TYPE_ID = "latch_hold";
		public static final Config INSTANCE = new Config();
		public static final MapCodec<Config> MAP_CODEC = MapCodec.unit(INSTANCE);

		@Override
		public HoldLatchActivator create(LogicalInput boundInput) {
			return new HoldLatchActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<HoldLatchActivator, Config> type() {
			return ActivatorConfigType.HOLD_LATCH;
		}
	}
}
