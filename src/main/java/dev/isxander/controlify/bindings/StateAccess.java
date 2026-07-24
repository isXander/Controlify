/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

public interface StateAccess {
	float analogue(int history);

	boolean digital(int history);

	boolean isSuppressed();

	boolean isValid();

	int maxHistory();
}
