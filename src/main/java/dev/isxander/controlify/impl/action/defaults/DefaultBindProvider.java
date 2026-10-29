/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.defaults;

import dev.isxander.controlify.api.action.ActionBinding;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface DefaultBindProvider {
	/// The default bindings for an action; empty when it is unbound by default.
	List<ActionBinding> getDefaultBindings(Identifier actionId);

	/// Whether this provider defines bindings, including an explicit unbinding.
	default boolean hasDefaultBindings(Identifier actionId) {
		return !this.getDefaultBindings(actionId).isEmpty();
	}

	DefaultBindProvider EMPTY = _ -> List.of();
}
