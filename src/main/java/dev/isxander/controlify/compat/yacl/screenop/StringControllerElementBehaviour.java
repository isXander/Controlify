/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.yacl.screenop;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.gui.keyboard.KeyboardLayouts;
import dev.isxander.controlify.api.gui.keyboard.KeyboardPositioner;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.compat.yacl.mixins.StringControllerElementAccessor;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import java.util.Optional;

public class StringControllerElementBehaviour implements ElementBehaviour {
	private final StringControllerElement element;

	public StringControllerElementBehaviour(StringControllerElement element) {
		this.element = element;
	}

	@Override
	public Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
		return Optional.of(new ElementKeyboardBehaviour.Handled(
				KeyboardLayouts.SIMPLE,
				new StringInputTarget(),
				KeyboardPositioner.aboveOrBelowWidgetPositioner(
						(int) (screen.screen().width * 0.8f), (int) (screen.screen().height * 0.4f),
						1,
						element::getRectangle
				)
		));
	}

	private class StringInputTarget implements InputTarget {
		@Override
		public boolean supportsCharInput() {
			return true;
		}

		@Override
		public boolean acceptChar(char ch, int modifiers) {
			return element.charTyped(new CharacterEvent(ch));
		}

		@Override
		public boolean supportsKeyCodeInput() {
			return true;
		}

		@Override
		public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
			return element.keyPressed(new KeyEvent(keycode, scancode, modifiers));
		}

		@Override
		public boolean supportsCursorMovement() {
			return true;
		}

		@Override
		public boolean moveCursor(int amount) {
			int keycode = amount > 0 ? InputConstants.KEY_RIGHT : InputConstants.KEY_LEFT;
			for (int i = 0; i < Math.abs(amount); i++) {
				element.keyPressed(new KeyEvent(keycode, 0, 0));
			}
			return true;
		}

		@Override
		public boolean supportsCopying() {
			return true;
		}

		@Override
		public boolean copy() {
			return ((StringControllerElementAccessor) element).callDoCopy();
		}
	}
}
