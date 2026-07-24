/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings.defaults;

import dev.isxander.controlify.bindings.input.Input;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface DefaultBindProvider {
	@Nullable Input getDefaultBind(Identifier bindId);

	DefaultBindProvider EMPTY = bind -> null;
}
