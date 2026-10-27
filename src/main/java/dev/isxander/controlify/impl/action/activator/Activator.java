/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

/// Processes signals and activates an {@link dev.isxander.controlify.api.action.Action}.
///
/// Activators form a tree. Leaves ({@link LeafSlot}) read the logical signals of the action's bindings;
/// every other activator transforms the output of its children and never reads inputs directly.
public interface Activator {
	/// Accepts a {@link LogicalSignal} and potentially submits state into the action accumulator.
	/// Activators wrapping others must forward every signal, including {@link LogicalSignal.Cancelled}.
	void onSignal(LogicalSignal signal, Accumulator acc);

	/// gives a string representation of this activator
	String describe();
}
