/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Processes signals and activates an {@link Action}.
public interface Activator {

	/// Accepts a {@link LogicalSignal} and potentially
	/// submits state into the action accumulator.
	/// Activators holding state for an in-progress press or axis must clear it on
	/// {@link LogicalSignal.Cancelled} without firing, as no release will follow.
	/// Activators wrapping others must forward it.
	void onSignal(LogicalSignal signal, Accumulator acc);

	/// The logical input this is bound to.
	LogicalInput boundInput();

	/// gives a string representation of this activator
	String describe();

}
