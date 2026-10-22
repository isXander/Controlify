/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.api.gui.keyboard.KeyboardPositioner;
import net.minecraft.resources.Identifier;

/**
 * Describes whether and how to open a keyboard when an element is pressed.
 * An absent policy is represented by Optional.empty(), allowing an underlying
 * element behaviour to supply one.
 */
public sealed interface ElementKeyboardBehaviour {

	/**
	 * Indicates that some other support has been provided, for example by the screen itself
	 * and a separate keyboard widget that supports it.
	 */
	record DoNothing() implements ElementKeyboardBehaviour {
	}

	/**
	 * Custom support for this element for a popup keyboard.
	 *
	 * @param layout      the keyboard layout to use
	 * @param inputTarget the input target for the keyboard
	 * @param positioner  the positioner for the overlaid keyboard
	 */
	record Handled(
			Identifier layout,
			InputTarget inputTarget,
			KeyboardPositioner positioner
	) implements ElementKeyboardBehaviour {
	}
}
