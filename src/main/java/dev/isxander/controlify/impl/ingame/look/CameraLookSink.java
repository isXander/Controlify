/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/// Turns the player's camera. This is the only place Minecraft's mouse sensitivity
/// factor appears: `LocalPlayer#turn` multiplies by 0.15 degrees per mouse unit.
public final class CameraLookSink implements LookSink {
	private static final double DEGREES_PER_MOUSE_UNIT = 0.15;

	private final Minecraft minecraft;

	public CameraLookSink(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void apply(double yawDeg, double pitchDeg) {
		LocalPlayer player = this.minecraft.player;
		if (player == null) {
			return;
		}

		double velX = yawDeg / DEGREES_PER_MOUSE_UNIT;
		double velY = pitchDeg / DEGREES_PER_MOUSE_UNIT;
		player.turn(velX, velY);
		this.minecraft.getTutorial().onMouse(velX, velY);
	}
}
