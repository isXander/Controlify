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

/// Produces a pulse on release when no GUI navigation consumed the press.
public record GuiPressPulseActivator(LogicalInput boundInput) implements Activator {
	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		if (signal instanceof LogicalSignal.GuiPress(_, LogicalInput input) && this.boundInput.equals(input)) {
			acc.firePulse();
		}
	}

	@Override
	public String describe() {
		return "GuiPress[" + boundInput.toString() + "]";
	}

	public record Config() implements ActivatorConfig<GuiPressPulseActivator> {
		public static final String TYPE_ID = "pulse_gui_press";
		public static final Config INSTANCE = new Config();

		@Override
		public GuiPressPulseActivator create(LogicalInput boundInput) {
			return new GuiPressPulseActivator(boundInput);
		}

		@Override
		public ActivatorConfigType<GuiPressPulseActivator, Config> type() {
			return ActivatorConfigType.GUI_PRESS_PULSE;
		}
	}
}
