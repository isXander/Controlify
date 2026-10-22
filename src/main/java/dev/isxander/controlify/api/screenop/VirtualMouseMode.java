/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

/// Screen-level cursor policy, independent of the middleware implementing it.
public enum VirtualMouseMode {
	/// Uses the saved screen preference and permits the user to toggle it.
	DEFAULT,
	ENABLED,
	DISABLED,
	CURSOR_ONLY,
	CURSOR_SCROLL;

	public boolean emulatesClicks() {
		return this == DEFAULT || this == ENABLED;
	}

	public boolean scrolls() {
		return emulatesClicks() || this == CURSOR_SCROLL;
	}
}
