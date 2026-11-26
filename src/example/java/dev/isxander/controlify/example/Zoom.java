/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example;

/// Zooms the camera in while the zoom key is held.
public final class Zoom {
	/// How much of the normal field of view is left while zoomed.
	public static final float FOV_MULTIPLIER = 0.3f;

	public static boolean isZooming() {
		return ExampleKeyMappings.ZOOM != null && ExampleKeyMappings.ZOOM.isDown();
	}

	private Zoom() {
	}
}
