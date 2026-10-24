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
