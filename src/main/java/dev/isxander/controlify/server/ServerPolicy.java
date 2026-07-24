/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.server;

public enum ServerPolicy {
	ALLOWED,
	DISALLOWED,
	UNSET;

	public boolean isAllowed() {
		return this != DISALLOWED;
	}

	public static ServerPolicy fromBoolean(boolean value) {
		return value ? ALLOWED : DISALLOWED;
	}
}
