/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.input.LogicalInput;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// An action is bound to any number of logical inputs and has an activator tree
/// that determines how its output channels respond to them.
public interface Action {
	ActionSpec spec();

	Channel.Pulse pulse();
	Channel.Latch latch();
	Channel.Continuous continuous();

	/// The current bindings, in display order. Empty when unbound.
	List<ActionBinding> bindings();

	/// Replaces every binding. An empty list unbinds the action.
	void setBindings(List<ActionBinding> bindings);

	/// The bindings this action has by default for the current controller type.
	List<ActionBinding> defaultBindings();

	/// The input shown in prompts and button glyphs: the first binding's, or null when unbound.
	default @Nullable LogicalInput primaryInput() {
		List<ActionBinding> bindings = this.bindings();
		return bindings.isEmpty() ? null : bindings.getFirst().input();
	}
}
