/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

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
