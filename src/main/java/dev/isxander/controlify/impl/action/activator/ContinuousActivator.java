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
