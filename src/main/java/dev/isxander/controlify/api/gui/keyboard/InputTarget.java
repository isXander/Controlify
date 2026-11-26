/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.gui.keyboard;

import com.mojang.blaze3d.platform.InputConstants;

/// Represents a target/consumer for keyboard input.
/// A target accepts input from the on-screen keyboard.
/// It can have various capabilities, all of which are optional to implement.
public interface InputTarget {
	InputTarget EMPTY = new InputTarget() {
	};

	default boolean supportsCharInput() {
		return false;
	}

	default boolean acceptChar(char ch, int modifiers) {
		return false;
	}

	default boolean supportsKeyCodeInput() {
		return false;
	}

	default boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
		return false;
	}

	default boolean supportsCopying() {
		return false;
	}

	default boolean copy() {
		return false;
	}

	default boolean supportsCursorMovement() {
		return false;
	}

	default boolean moveCursor(int amount) {
		return false;
	}

	/// Whether the target can be told the player is done typing, from the keyboard's done key.
	/// By default, a target that accepts key codes supports it.
	default boolean supportsDone() {
		return this.supportsKeyCodeInput();
	}

	/// The player is done typing. What that means is up to the target: chat sends the message,
	/// and a sign saves and closes its editor.
	/// By default, this presses Enter.
	///
	/// @return whether the target handled it
	default boolean done() {
		return this.acceptKeyCode(InputConstants.KEY_RETURN, 0, 0);
	}

}
