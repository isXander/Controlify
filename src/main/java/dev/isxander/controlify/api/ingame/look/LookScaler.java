/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

/// Scales the look contributions each frame. All scales multiply, so registration order never
/// matters. One instance exists per controller; see [LookEvents#REGISTER].
@FunctionalInterface
public interface LookScaler {
	/// Called once per frame while look input can be processed.
	void scale(LookContext ctx, LookScales out);
}
