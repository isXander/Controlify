/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.action.Activator;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Accepts another pulse binding, and converts pulses into toggling a latch.
public class ToggleLatchActivator implements Activator {

	private final Activator pulseActivator;
	private final PulseToToggleAcc pulseToToggleAcc;

	public ToggleLatchActivator(Activator pulseActivator) {
		this.pulseActivator = pulseActivator;
		this.pulseToToggleAcc = new PulseToToggleAcc();
	}

	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		this.pulseToToggleAcc.target = acc;
		this.pulseActivator.onSignal(signal, this.pulseToToggleAcc);
	}

	@Override
	public LogicalInput boundInput() {
		return this.pulseActivator.boundInput();
	}

	@Override
	public String describe() {
		return "Toggle[" + this.pulseActivator.describe() + "]";
	}

	private static final class PulseToToggleAcc implements Accumulator {
		private Accumulator target;

		@Override
		public void firePulse() {
			target.toggleLatch();
		}
	}

	public record Config(
			ActivatorConfig<?> pulseBinding
	) implements ActivatorConfig<ToggleLatchActivator> {
		public static final String TYPE_ID = "latch_toggle";
		public static final MapCodec<Config> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ActivatorConfig.CODEC.fieldOf("source_pulse").forGetter(Config::pulseBinding)
		).apply(instance, Config::new));

		@Override
		public ToggleLatchActivator create(LogicalInput boundInput) {
			return new ToggleLatchActivator(this.pulseBinding.create(boundInput));
		}

		@Override
		public ActivatorConfigType<ToggleLatchActivator, Config> type() {
			return ActivatorConfigType.TOGGLE_LATCH;
		}
	}
}
