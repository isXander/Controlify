/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.keyboard;

import dev.isxander.controlify.api.gui.keyboard.InputTarget;

public class DelegatingInputTarget implements InputTarget {
	private final InputTarget target;

	public DelegatingInputTarget(InputTarget target) {
		this.target = target;
	}

	@Override
	public boolean supportsCharInput() {
		return this.target.supportsCharInput();
	}

	@Override
	public boolean acceptChar(char ch, int modifiers) {
		return this.target.acceptChar(ch, modifiers);
	}

	@Override
	public boolean supportsKeyCodeInput() {
		return this.target.supportsKeyCodeInput();
	}

	@Override
	public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
		return this.target.acceptKeyCode(keycode, scancode, modifiers);
	}

	@Override
	public boolean supportsCopying() {
		return this.target.supportsCopying();
	}

	@Override
	public boolean copy() {
		return this.target.copy();
	}

	@Override
	public boolean supportsCursorMovement() {
		return this.target.supportsCursorMovement();
	}

	@Override
	public boolean moveCursor(int amount) {
		return this.target.moveCursor(amount);
	}
}
