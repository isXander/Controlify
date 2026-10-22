package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.action.Activator;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Emits a pulse immediately when the source latch becomes active, then repeats while held.
/// initialDelayMs delays the first repeat, not the initial pulse.
/// Do not combine with a press-pulse activator unless two initial pulses are intended.
public class RepeatLatchPulseActivator implements Activator {

	private final Activator latchActivator;
	private final long initialDelayNs, repeatDelayNs;
	private final HoldRepeatAccumulator holdRepeatAccumulator;

	private boolean down;
	private long nextDue;

	public RepeatLatchPulseActivator(
			Activator latchActivator,
			long initialDelayNs, long repeatDelayNs
	) {
		this.latchActivator = latchActivator;
		this.initialDelayNs = initialDelayNs;
		this.repeatDelayNs = repeatDelayNs;
		this.holdRepeatAccumulator = new HoldRepeatAccumulator();
	}

	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		this.holdRepeatAccumulator.target = acc;
		this.holdRepeatAccumulator.time = signal.timeNanos();

		this.latchActivator.onSignal(signal, this.holdRepeatAccumulator);

		if (signal instanceof LogicalSignal.Tick) {
			while (down && signal.timeNanos() >= nextDue) {
				acc.firePulse();
				nextDue += repeatDelayNs;
			}
		}
	}

	@Override
	public LogicalInput boundInput() {
		return this.latchActivator.boundInput();
	}

	@Override
	public String describe() {
		return "HoldRepeat[" + latchActivator.describe() + ", initialDelay=" + initialDelayNs + "ns, repeatDelay=" + repeatDelayNs + "ns]";
	}

	private class HoldRepeatAccumulator implements Accumulator {
		private Accumulator target;
		private long time;

		@Override
		public void setLatch(boolean active) {
			if (active) {
				down = true;
				nextDue = time + initialDelayNs;
				target.firePulse();
			} else {
				down = false;
				nextDue = Long.MAX_VALUE;
			}
		}

		@Override
		public void toggleLatch() {
			this.setLatch(!down);
		}
	}

	public record Config(
			ActivatorConfig<?> latchBinding,
			int initialDelayMs, int repeatDelayMs
	) implements ActivatorConfig<RepeatLatchPulseActivator> {
		public static final String TYPE_ID = "pulse_repeat_latch";
		public static final MapCodec<Config> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ActivatorConfig.CODEC.fieldOf("source_latch").forGetter(Config::latchBinding),
				Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("initial_delay_ms", 5).forGetter(Config::initialDelayMs),
				Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("repeat_delay_ms", 1).forGetter(Config::repeatDelayMs)
		).apply(instance, Config::new));

		@Override
		public RepeatLatchPulseActivator create(LogicalInput boundInput) {
			long initialDelayNs = this.initialDelayMs * 1_000_000L;
			long repeatDelayNs = this.repeatDelayMs * 1_000_000L;
			return new RepeatLatchPulseActivator(this.latchBinding.create(boundInput), initialDelayNs, repeatDelayNs);
		}

		@Override
		public ActivatorConfigType<RepeatLatchPulseActivator, Config> type() {
			return ActivatorConfigType.REPEAT_LATCH_PULSE;
		}
	}
}
