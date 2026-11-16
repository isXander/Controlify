/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.player.LocalPlayer;

/// What a look frame knows. Built once per frame and shared by every source and scaler.
///
/// @param controller the controller whose look input is being processed
/// @param player the player being turned
/// @param dt the frame time in seconds
/// @param yaw the camera yaw at the start of the frame, in degrees
/// @param pitch the camera pitch at the start of the frame, in degrees
public record LookContext(
		Controller controller,
		LocalPlayer player,
		double dt,
		float yaw,
		float pitch
) {
}
