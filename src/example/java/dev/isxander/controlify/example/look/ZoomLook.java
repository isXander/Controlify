/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.look;

import dev.isxander.controlify.api.ingame.look.LookEvents;
import dev.isxander.controlify.example.Zoom;

import static dev.isxander.controlify.example.ExampleMod.id;

/// Slows controller look while zoomed, so aiming feels the same as unzoomed.
/// Mouse look is the mod's own job, since it has to work without Controlify.
public final class ZoomLook {
	public static void registerEvents() {
		// #region scaler
		LookEvents.REGISTER.subscribeForever(registry -> registry.registerScaler(id("zoom"), controller -> (ctx, scales) -> {
			if (Zoom.isZooming()) {
				scales.rate(Zoom.FOV_MULTIPLIER, Zoom.FOV_MULTIPLIER);
				scales.direct(Zoom.FOV_MULTIPLIER, Zoom.FOV_MULTIPLIER);
			}
		}));
		// #endregion scaler
	}

	private ZoomLook() {
	}
}
