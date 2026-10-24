/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

/// Screen-level policy selecting cursor movement, scrolling, and automatic click emulation.
/// The mode is configured through [VirtualMouse#setMode] and is distinct from the live
/// [VirtualMouse#isEnabled] state. All modes still respect screen activation, controller
/// input mode, and temporary cursor suspension.
///
/// Only [#DEFAULT] follows the saved screen preference and permits user toggling;
/// the other constants select a fixed policy for that screen context.
public enum VirtualMouseMode {
	/// Follows the saved preference for this screen type and permits the user to toggle it.
	/// When enabled, supplies movement, snapping, scrolling, and automatic click emulation.
	/// This is a distinct selectable policy, not a placeholder for another enum value.
	DEFAULT,

	/// Requests movement, snapping, scrolling, and automatic click emulation without
	/// allowing the user toggle to change this screen's policy.
	ENABLED,

	/// Disables the virtual mouse for this screen, leaving focus-based controls available.
	DISABLED,

	/// Requests movement and snapping without automatic scrolling or click emulation.
	/// Screen-specific behaviours can interpret interactions or call [VirtualMouse#emulateClicks]
	/// explicitly when they need a mouse fallback.
	CURSOR_ONLY,

	/// Requests movement, snapping, and scrolling without automatic click emulation.
	/// Screen-specific behaviours handle interaction, optionally using
	/// [VirtualMouse#emulateClicks] as an explicit fallback.
	CURSOR_SCROLL;

	/// Reports whether this policy includes automatic mouse-button emulation.
	/// This describes the policy alone, so [#DEFAULT] returns true even when the saved
	/// preference currently leaves the cursor disabled.
	///
	/// @return true for [#DEFAULT] and [#ENABLED]
	public boolean emulatesClicks() {
		return this == DEFAULT || this == ENABLED;
	}

	/// Reports whether this policy includes automatic cursor scrolling.
	/// This describes the policy alone and does not indicate that the cursor is enabled.
	///
	/// @return true for [#DEFAULT], [#ENABLED], and [#CURSOR_SCROLL]
	public boolean scrolls() {
		return emulatesClicks() || this == CURSOR_SCROLL;
	}
}
