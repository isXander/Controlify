/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import dev.isxander.controlify.api.ingame.look.LookContext;
import dev.isxander.controlify.api.ingame.look.LookOutput;
import dev.isxander.controlify.api.ingame.look.LookSource;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.config.settings.profile.GyroSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.capsense.CapSenseComponent;
import dev.isxander.controlify.impl.controller.gyro.GyroActivation;
import dev.isxander.controlify.impl.controller.gyro.GyroComponentImpl;
import net.minecraft.resources.Identifier;
import org.joml.Vector2fc;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/// Gyro look: the processed gyro displacement, emitted 1:1 as a direct contribution.
public final class GyroLook implements LookSource {
	private final ControllerImpl controller;

	public GyroLook(ControllerImpl controller) {
		this.controller = controller;
	}

	@Override
	public void contribute(@NonNull LookContext ctx, @NonNull LookOutput out) {
		GyroComponentImpl gyro = controller.gyro().orElse(null);
		if (gyro == null) {
			return;
		}
		GyroSettings settings = gyro.settings();

		// pulses are consumed every frame, so they never pile up while gyro is inactive
		var gyroButton = ControlifyActions.GYRO_BUTTON.on(controller);
		while (gyroButton.pulse().consumePulse()) {
			if (settings.activation == GyroActivation.TOGGLE) {
				gyro.setToggledOn(!gyro.isToggledOn());
			}
		}

		if (!settings.enabled) {
			return;
		}

		boolean aiming = AimingItems.isAiming(ctx.player().getUseItem().getUseAnimation());
		boolean held = gyroButton.latch().isLatchActive() || this.anyTouched(settings);
		if (!isActive(settings.activation, held, gyro.isToggledOn(), aiming, settings.activeWhileAiming)) {
			return;
		}

		float multiplier = aiming ? settings.aimMultiplier : 1f;
		Vector2fc look = gyro.lookDelta();
		out.delta(
				look.x() * multiplier * (settings.invertYaw ? -1 : 1),
				look.y() * multiplier * (settings.invertPitch ? -1 : 1)
		);
	}

	private boolean anyTouched(GyroSettings settings) {
		if (settings.activation == GyroActivation.TOGGLE || settings.touchActivators.isEmpty()) {
			return false;
		}
		Optional<CapSenseComponent> capSense = controller.capSense();
		if (capSense.isEmpty()) {
			return false;
		}
		for (Identifier sensor : settings.touchActivators) {
			if (capSense.get().isTouched(sensor)) {
				return true;
			}
		}
		return false;
	}

	/// @param held the gyro button is held or a touch activator is touched
	/// @param toggledOn the toggle state, only used by [GyroActivation#TOGGLE]
	/// @return whether gyro look applies this frame
	public static boolean isActive(
			GyroActivation activation,
			boolean held,
			boolean toggledOn,
			boolean aiming,
			boolean activeWhileAiming
	) {
		if (activeWhileAiming && aiming) {
			return true;
		}
		return switch (activation) {
			case ALWAYS -> true;
			case HOLD_ON -> held;
			case HOLD_OFF -> !held;
			case TOGGLE -> toggledOn;
		};
	}

	// reset() deliberately keeps the toggle state, which lives on the gyro component so it also
	// survives the in-game input handler being rebuilt: it is the user's choice, not transient input.
}
