/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.gui.navigation.ScreenDirection;

/// Shared cursor state and operations for one [ScreenContext].
/// The [mode][#mode()] is the screen's policy; [#isEnabled()] reports
/// whether the cursor is actually running given that policy, controller input mode,
/// screen activation, and temporary suspension. Configure a mode in the screen factory
/// and share this instance between behaviours that need cursor operations.
///
/// Cursor positions returned here use window mouse coordinates, before GUI scaling.
/// [SnapPoint] positions instead use scaled GUI coordinates; snapping performs the
/// conversion. Automatic movement, scrolling, and clicks are provided by screen behaviours.
public interface VirtualMouse {
	/// Returns the configured policy, which can differ from the current enabled state.
	/// [VirtualMouseMode#DEFAULT] is a distinct policy that follows the saved screen
	/// preference and allows user toggling.
	///
	/// @return this screen's virtual mouse mode
	VirtualMouseMode mode();

	/// Changes the screen's cursor policy and synchronizes its enabled state.
	/// This is an explicit choice, including when selecting [VirtualMouseMode#DEFAULT].
	/// Changing modes releases held emulated mouse buttons and clears queued scrolling when
	/// the new mode does not support it. The mode is retained by this screen context.
	///
	/// @param mode the policy to apply
	void setMode(VirtualMouseMode mode);

	/// Reports whether this cursor is currently active.
	/// An enabled policy alone is insufficient: the screen must be active, controller input
	/// must be in use with a current controller, and the cursor must not be suspended.
	///
	/// @return whether virtual mouse behaviours should currently operate
	boolean isEnabled();

	/// Returns the interpolated horizontal cursor position in window mouse coordinates.
	/// This is an unscaled coordinate, not a [SnapPoint] GUI coordinate.
	///
	/// @param deltaTime interpolation fraction from the current position to its target,
	///   normally between zero and one
	/// @return the interpolated x coordinate, truncated to an integer
	int getCurrentX(float deltaTime);

	/// Returns the interpolated vertical cursor position in window mouse coordinates.
	/// This is an unscaled coordinate, not a [SnapPoint] GUI coordinate.
	///
	/// @param deltaTime interpolation fraction from the current position to its target,
	///   normally between zero and one
	/// @return the interpolated y coordinate, truncated to an integer
	int getCurrentY(float deltaTime);

	/// Refreshes this screen's snap points and snaps to the closest eligible point within
	/// its capture range. A previously snapped point is excluded until the cursor leaves
	/// its range. Does nothing if no target qualifies or this cursor is not enabled on the
	/// current screen.
	void snapToClosestPoint();

	/// Refreshes snap points and moves to a suitable target in the supplied direction.
	/// Directional selection favours nearby targets aligned with that direction and can
	/// wrap from the opposite screen edge when no forward target qualifies. Capture ranges
	/// do not act as the same proximity limit used by [#snapToClosestPoint()].
	/// Does nothing if no target qualifies or this cursor is not enabled on the current screen.
	///
	/// @param direction the direction in scaled screen space
	void snapInDirection(ScreenDirection direction);

	/// Processes virtual mouse click actions explicitly, including when the selected mode
	/// disables automatic click emulation. Left and right presses consume accumulated press
	/// pulses and remain held until their actions are released; shift-click pulses produce
	/// individual press/release pairs.
	///
	/// Use this as a fallback for screen-specific interaction. The cursor must still be
	/// enabled on the current screen. Consumed pulses cannot also be handled by another
	/// click behaviour during the same invocation.
	///
	/// @param controller the controller providing click actions
	void emulateClicks(Controller controller);

	/// Discards the cursor's currently queued scroll movement on both axes.
	/// Call after navigation has queued scrolling to suppress that movement before the next
	/// frame applies it. This does not consume scroll action state or prevent a later
	/// behaviour from queuing more movement.
	void preventScrollingThisTick();
}
