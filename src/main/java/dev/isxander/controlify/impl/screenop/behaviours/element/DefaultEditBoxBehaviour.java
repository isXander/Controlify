/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.element;

import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.gui.keyboard.KeyboardLayouts;
import dev.isxander.controlify.api.gui.keyboard.KeyboardPositioner;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.gui.keyboard.EditBoxInputTarget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class DefaultEditBoxBehaviour implements ElementBehaviour {
	private final EditBox editBox;
	private final Identifier layout;
	private final InputTarget inputTarget;

	public DefaultEditBoxBehaviour(EditBox editBox) {
		this(editBox, KeyboardLayouts.SIMPLE, new EditBoxInputTarget(editBox));
	}

	public DefaultEditBoxBehaviour(EditBox editBox, Identifier layout, InputTarget inputTarget) {
		this.editBox = editBox;
		this.layout = layout;
		this.inputTarget = inputTarget;
	}

	@Override
	public Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
		return Optional.of(new ElementKeyboardBehaviour.Handled(
				layout, inputTarget,
				KeyboardPositioner.aboveOrBelowWidgetPositioner(
						(int) (screen.screen().width * 0.8f), (int) (screen.screen().height * 0.4f),
						1, editBox::getRectangle)));
	}
}
