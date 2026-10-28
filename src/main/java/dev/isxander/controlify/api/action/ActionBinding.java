/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.input.LogicalInput;

import java.util.Optional;

/// One input bound to an action.
/// @param input the input that activates the action
/// @param gesture overrides the gesture of the action's editable activator for this binding, if it has one
///                of the same {@link Gesture.Kind}; ignored otherwise
public record ActionBinding(LogicalInput input, Optional<Gesture> gesture) {
	public ActionBinding(LogicalInput input) {
		this(input, Optional.empty());
	}

	public static ActionBinding of(LogicalInput input) {
		return new ActionBinding(input);
	}

	public ActionBinding withGesture(Optional<Gesture> gesture) {
		return new ActionBinding(this.input, gesture);
	}
}
