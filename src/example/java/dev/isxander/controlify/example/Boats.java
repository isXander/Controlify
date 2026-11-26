/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example;

import dev.isxander.controlify.example.settings.BoatSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/// The boating features, shared by keyboard and controller input.
public final class Boats {
	/// How many ticks of boost a full tank holds. It refills one tick at a time while not boosting.
	private static final int MAX_BOOST_TICKS = 60;
	/// How fast the boat can turn towards where its driver is looking, in degrees per tick at full throttle.
	private static final float MAX_TURN_PER_TICK = 4f;

	private static int boostTicksLeft = MAX_BOOST_TICKS;
	private static @Nullable AbstractBoat boostedBoat;
	private static long lastBoostTick = Long.MIN_VALUE;

	public static @Nullable AbstractBoat currentBoat(Minecraft minecraft) {
		return minecraft.player != null && minecraft.player.getVehicle() instanceof AbstractBoat boat ? boat : null;
	}

	public static void tick(Minecraft minecraft) {
		boolean boating = currentBoat(minecraft) != null;
		while (ExampleKeyMappings.HORN.consumeClick()) {
			if (boating) {
				honk(minecraft);
			}
		}
		while (ExampleKeyMappings.SETTINGS.consumeClick()) {
			Screens.set(new BoatSettingsScreen(Screens.current()));
		}

		if (minecraft.level != null && minecraft.level.getGameTime() > lastBoostTick && boostTicksLeft < MAX_BOOST_TICKS) {
			boostTicksLeft++;
		}
	}

	public static void honk(Minecraft minecraft) {
		if (minecraft.player != null) {
			minecraft.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 0.5f);
		}
	}

	/// Speeds the boat up, if there is boost left.
	public static void boost(AbstractBoat boat) {
		if (boostTicksLeft <= 0) {
			return;
		}
		boostTicksLeft--;
		boostedBoat = boat;
		lastBoostTick = boat.level().getGameTime();
		boat.setDeltaMovement(boat.getDeltaMovement().multiply(1.05, 1, 1.05));
	}

	/// Whether the player's boat boosted this tick or the last.
	public static boolean isPlayerInBoostingBoat(Player player) {
		return player.getVehicle() instanceof AbstractBoat boat
				&& boat == boostedBoat
				&& boat.level().getGameTime() - lastBoostTick <= 1;
	}

	/// Pushes the boat forwards, steering it towards where its driver is looking.
	public static void accelerate(AbstractBoat boat, float amount) {
		if (boat.getControllingPassenger() instanceof Player driver) {
			float turn = Mth.degreesDifference(boat.getYRot(), driver.getYRot());
			boat.setYRot(boat.getYRot() + Math.clamp(turn, -MAX_TURN_PER_TICK, MAX_TURN_PER_TICK) * amount);
		}
		Vec3 forward = Vec3.directionFromRotation(0, boat.getYRot());
		boat.setDeltaMovement(boat.getDeltaMovement().add(forward.scale(0.04 * amount)));
	}

	private Boats() {
	}
}
