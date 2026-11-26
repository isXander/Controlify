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

/// Policy describing whether and how an element handles on-screen keyboard activation.
/// [ElementBehaviour#keyboardBehaviour] returns an optional policy: an empty result
/// inherits the next behaviour's policy, while either variant here explicitly handles
/// activation. The first present policy in an element's chain wins.
///
/// Policies are interpreted by [ScreenContext#tryOpenKeyboard]; merely returning one
/// does not open a keyboard or consume action pulses.
public sealed interface ElementKeyboardBehaviour {

	/// Handles keyboard activation without opening a popup or requesting ordinary activation.
	/// Use this when the screen already supplies keyboard support, such as an embedded
	/// keyboard widget. This explicit policy wins over later policies; it differs from an
	/// empty optional, which delegates to them.
	record DoNothing() implements ElementKeyboardBehaviour {
	}

	/// Handles activation by requesting a popup keyboard for the element.
	/// The user's on-screen keyboard setting determines whether the popup actually opens;
	/// activation is still considered handled when that setting disables it.
	///
	/// @param layout the resource identifier of the keyboard layout to load
	/// @param inputTarget receives text and editing input from the popup keyboard
	/// @param positioner positions the popup relative to the parent screen
	record Handled(
			Identifier layout,
			InputTarget inputTarget,
			KeyboardPositioner positioner
	) implements ElementKeyboardBehaviour {
	}
}
