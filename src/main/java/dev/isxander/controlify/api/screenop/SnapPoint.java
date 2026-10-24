/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import org.joml.Vector2i;
import org.joml.Vector2ic;

/// A target for virtual mouse snapping in scaled GUI coordinates.
/// Nearby snapping uses `range` as the capture radius around `position`; directional
/// snapping uses the targets' geometry to select a destination. These units differ from
/// the unscaled window coordinates returned by [VirtualMouse#getCurrentX] and
/// [VirtualMouse#getCurrentY].
///
/// The supplied position is retained. Treat its contents as immutable while the point
/// is in use so its geometry and equality remain stable during collection.
///
/// @param position the target position in scaled GUI pixels
/// @param range the capture radius in scaled GUI pixels
public record SnapPoint(Vector2ic position, int range) {
	/// Creates a snap point from scaled GUI coordinates, allocating its position vector.
	///
	/// @param x the target's horizontal position in scaled GUI pixels
	/// @param y the target's vertical position in scaled GUI pixels
	/// @param range the capture radius in scaled GUI pixels
	public SnapPoint(int x, int y, int range) {
		this(new Vector2i(x, y), range);
	}
}
