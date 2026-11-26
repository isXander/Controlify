/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.look;

import dev.isxander.controlify.api.ingame.look.LookContext;
import dev.isxander.controlify.api.ingame.look.LookEvents;
import dev.isxander.controlify.api.ingame.look.LookOutput;
import dev.isxander.controlify.api.ingame.look.LookSource;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.controller.gyro.GyroComponent;
import dev.isxander.controlify.example.settings.BoatSettings;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import static dev.isxander.controlify.example.ExampleMod.id;

// #region source
/// Tilting the controller like a steering wheel turns the camera while in a boat.
/// The boat steers towards where its driver looks, so this steers the boat.
public class TiltSteeringLook implements LookSource {
	/// Tilts smaller than this, in degrees, are ignored, so holding the controller roughly level doesn't turn.
	private static final double DEADZONE = 5.0;
	/// Degrees per second the camera turns for each degree of tilt, at full sensitivity.
	private static final double MAX_SENSITIVITY = 4.0;

	private final Controller controller;

	public TiltSteeringLook(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void contribute(LookContext ctx, LookOutput out) {
		if (!(ctx.player().getVehicle() instanceof AbstractBoat)) {
			return;
		}
		this.controller.gyro().flatMap(GyroComponent::gravity).ifPresent(up -> {
			// Tilting the controller right makes "up" lean towards its left, the negative X axis.
			double tilt = Math.toDegrees(Math.asin(Math.clamp(-up.x(), -1.0, 1.0)));
			if (Math.abs(tilt) < DEADZONE) {
				return;
			}
			double steer = tilt - Math.copySign(DEADZONE, tilt);
			out.rate(steer * BoatSettings.tiltSensitivity * MAX_SENSITIVITY, 0);
		});
	}

	public static void registerEvents() {
		LookEvents.REGISTER.subscribeForever(registry -> registry.registerSource(id("tilt_steering"), TiltSteeringLook::new));
	}
}
// #endregion source
