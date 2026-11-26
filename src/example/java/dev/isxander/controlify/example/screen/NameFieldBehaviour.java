/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.gui.keyboard.KeyboardLayouts;
import dev.isxander.controlify.api.gui.keyboard.KeyboardPositioner;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.example.settings.NameField;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

// #region keyboard
/// Opens the on-screen keyboard for the name field.
public record NameFieldBehaviour(NameField field) implements ElementBehaviour {
	@Override
	public @NonNull Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
		return Optional.of(new ElementKeyboardBehaviour.Handled(
				KeyboardLayouts.SIMPLE,
				new NameFieldInputTarget(this.field),
				KeyboardPositioner.aboveOrBelowWidgetPositioner(
						screen.screen().width * 4 / 5, screen.screen().height * 2 / 5, 1, this.field::getRectangle)
		));
	}
}
// #endregion keyboard

// #region input-target
/// Types the on-screen keyboard's input into the name field.
record NameFieldInputTarget(NameField field) implements InputTarget {
	@Override
	public boolean supportsCharInput() {
		return true;
	}

	@Override
	public boolean acceptChar(char ch, int modifiers) {
		this.field.insert(String.valueOf(ch));
		return true;
	}

	@Override
	public boolean supportsKeyCodeInput() {
		return true;
	}

	@Override
	public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
		if (keycode == InputConstants.KEY_BACKSPACE) {
			this.field.deleteLast();
			return true;
		}
		return false;
	}
}
// #endregion input-target
