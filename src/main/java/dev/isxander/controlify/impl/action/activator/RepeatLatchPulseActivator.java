/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

import java.util.List;

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
		if (initialDelayNs < 0 || repeatDelayNs <= 0) {
			throw new IllegalArgumentException("Repeat delays must be positive and initial delay nonnegative");
		}
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

		if (signal instanceof LogicalSignal.Frame) {
			if (down && signal.timeNanos() >= nextDue) {
				acc.firePulse();
				// Skip missed repeats after a hitch while preserving the repeat cadence.
				nextDue = signal.timeNanos() + repeatDelayNs - (signal.timeNanos() - nextDue) % repeatDelayNs;
			}
		}
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
			if (active == down) {
				return;
			}
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
				Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("initial_delay_ms", 250).forGetter(Config::initialDelayMs),
				Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("repeat_delay_ms", 50).forGetter(Config::repeatDelayMs)
		).apply(instance, Config::new));

		@Override
		public RepeatLatchPulseActivator create(List<ActionBinding> bindings) {
			long initialDelayNs = this.initialDelayMs * 1_000_000L;
			long repeatDelayNs = this.repeatDelayMs * 1_000_000L;
			return new RepeatLatchPulseActivator(this.latchBinding.create(bindings), initialDelayNs, repeatDelayNs);
		}

		@Override
		public List<LeafConfig> leaves() {
			return this.latchBinding.leaves();
		}

		@Override
		public ActivatorConfigType<RepeatLatchPulseActivator, Config> type() {
			return ActivatorConfigType.REPEAT_LATCH_PULSE;
		}
	}
}
