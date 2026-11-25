/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

import net.minecraft.client.input.KeyEvent;

public final class KeyEvents {
	private KeyEvents() {
	}

	/// Synthesises a key press. From 26.3 the second component of a key event is the keycode,
	/// which widgets such as edit boxes switch on, so it must match the key rather than be zero.
	public static KeyEvent of(int key, int scancode, int modifiers) {
		//? if >=26.3 {
		return new KeyEvent(key, key, modifiers);
		//?} else {
		//return new KeyEvent(key, scancode, modifiers);
		//?}
	}
}
