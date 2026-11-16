/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

/// Adds to the camera rotation each frame. Sources are summed, so registration order never
/// matters. One instance exists per controller; see [LookEvents#REGISTER].
public interface LookSource {
	/// Called once per frame while look input can be processed.
	void contribute(LookContext ctx, LookOutput out);

	/// Look processing was interrupted, such as a screen opening or the window losing focus.
	/// Drop transient state, like a rotation still being released.
	default void reset() {
	}
}
