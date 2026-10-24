/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.action.Activator;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Produces a pulse when the input is pressed.
public record PressPulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.ButtonPress(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "Press[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<PressPulseActivator> {
		public static final String TYPE_ID = "pulse_press";
		public static final Config INSTANCE = new Config();

		@Override
		public PressPulseActivator create(LogicalInput boundInput) {
			return new PressPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<PressPulseActivator, Config> type() {
			return ActivatorConfigType.PRESS_PULSE;
		}
	}
}
