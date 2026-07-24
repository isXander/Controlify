/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.virtualmouse;

public enum VirtualMouseBehaviour {
	DEFAULT,
	ENABLED,
	DISABLED,
	CURSOR_ONLY,
	CURSOR_SCROLL;

	public boolean hasCursor() {
		return this != DISABLED;
	}

	public boolean isDefaultOr(VirtualMouseBehaviour behaviour) {
		return this == DEFAULT || this == behaviour;
	}
}
