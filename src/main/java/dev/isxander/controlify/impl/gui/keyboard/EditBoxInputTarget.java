/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.keyboard;

import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public record EditBoxInputTarget(EditBox editBox) implements InputTarget {

	@Override
	public boolean supportsCharInput() {
		return true;
	}

	@Override
	public boolean acceptChar(char ch, int modifiers) {
		this.editBox.charTyped(new CharacterEvent(ch));
		return true;
	}

	@Override
	public boolean supportsKeyCodeInput() {
		return true;
	}

	@Override
	public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
		this.editBox.keyPressed(new KeyEvent(keycode, scancode, modifiers));
		return true;
	}

	@Override
	public boolean supportsCursorMovement() {
		return true;
	}

	@Override
	public boolean moveCursor(int amount) {
		this.editBox.moveCursor(amount, false);
		return true;
	}

	@Override
	public boolean supportsCopying() {
		return true;
	}

	@Override
	public boolean copy() {
		Minecraft.getInstance().keyboardHandler.setClipboard(this.editBox.getValue());
		return true;
	}
}
