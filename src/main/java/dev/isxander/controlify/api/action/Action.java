/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.input.LogicalInput;
import org.jspecify.annotations.Nullable;

/// An action is bound to a logical input and has one or more activators
/// that determine how its output channels respond to input signals.
public interface Action {
	ActionSpec spec();

	Channel.Pulse pulse();
	Channel.Latch latch();
	Channel.Continuous continuous();

	/// When set to null, it is unbound.
	void setBoundInput(@Nullable LogicalInput input);

	/// When set to null, it is currently unbound.
	@Nullable LogicalInput boundInput();

	/// When set to null, it is by default unbound.
	@Nullable LogicalInput defaultInput();
}
