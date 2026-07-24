/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify;

public enum InputMode {
	KEYBOARD_MOUSE,
	CONTROLLER,
	MIXED;

	public boolean isKeyboardMouse() {
		return this != CONTROLLER;
	}

	public boolean isController() {
		return this != KEYBOARD_MOUSE;
	}
}
