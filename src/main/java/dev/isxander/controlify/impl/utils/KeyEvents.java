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

	/// Synthesises a key press. From 26.3 the first component of a key event is an SDL scancode
	/// (which is what `InputConstants.KEY_*` are) and the second is the SDL keycode that widgets
	/// such as edit boxes switch on, so the keycode must be derived from the scancode.
	public static KeyEvent of(int key, int scancode, int modifiers) {
		//? if >=26.3 {
		return new KeyEvent(key, org.lwjgl.sdl.SDLKeyboard.SDL_GetKeyFromScancode(key, (short) 0, false), modifiers);
		//?} else {
		//return new KeyEvent(key, scancode, modifiers);
		//?}
	}
}
