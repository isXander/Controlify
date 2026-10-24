/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.defaults;

import dev.isxander.controlify.api.input.LogicalInput;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface DefaultBindProvider {
	@Nullable LogicalInput getDefaultInput(Identifier actionId);

	/// Whether this provider defines an input, including an explicit unbinding.
	default boolean hasDefaultInput(Identifier actionId) {
		return this.getDefaultInput(actionId) != null;
	}

	DefaultBindProvider EMPTY = bind -> null;
}
