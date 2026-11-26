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
import dev.isxander.controlify.impl.config.settings.profile.InputSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.utils.ControllerUtils;
import net.minecraft.world.item.ItemUseAnimation;
import org.joml.Vector2d;
import org.jspecify.annotations.NonNull;

/// Look with the right stick: a velocity, so it is emitted as [LookOutput#rate].
public final class StickLook implements LookSource {
	// This roughly matches Bedrock Edition's default 100% sensitivity
	private static final double DEGREES_PER_SECOND_AT_FULL_SENSITIVITY = 10.0 * 20.0;

	private final ControllerImpl controller;

	public StickLook(ControllerImpl controller) {
		this.controller = controller;
	}

	@Override
	public void contribute(@NonNull LookContext ctx, @NonNull LookOutput out) {
		if (FlickStick.isEnabled(controller)) {
			return;
		}

		InputSettings settings = controller.input().settings();

		// normal look input
		Vector2d impulse = new Vector2d(
				ControlifyActions.LOOK_RIGHT.on(controller).continuous().getContinuous()
						- ControlifyActions.LOOK_LEFT.on(controller).continuous().getContinuous(),
				ControlifyActions.LOOK_DOWN.on(controller).continuous().getContinuous()
						- ControlifyActions.LOOK_UP.on(controller).continuous().getContinuous()
		);
		if (settings.sensitivity.hLookInvert) {
			impulse.x *= -1;
		}
		if (settings.sensitivity.vLookInvert) {
			impulse.y *= -1;
		}

		if (!settings.sensitivity.isLCE) {
			// apply the easing on its length to preserve circularity
			impulse = ControllerUtils.applyEasingToLength(
					impulse,
					settings.sensitivity.lookInputCurve::apply
			);
		} else {
			// LCE uses a quadratic curve on each axis independently,
			// and its default turn speed was 7.5 degrees per tick at 100% sensitivity
			impulse.x = impulse.x * Math.abs(impulse.x) * 0.75f;
			impulse.y = impulse.y * Math.abs(impulse.y) * 0.75f;
		}

		if (settings.sensitivity.reduceAimingSensitivity) {
			ItemUseAnimation animation = ctx.player().getUseItem().getUseAnimation();
			float aimMultiplier = !AimingItems.isAiming(animation)
					? 1f
					: settings.sensitivity.isLCE
							? 0.15f
							: animation == ItemUseAnimation.SPYGLASS ? 0.2f : 0.6f;
			impulse.mul(aimMultiplier);
		}

		out.rate(
				impulse.x * settings.sensitivity.hLookSensitivity * DEGREES_PER_SECOND_AT_FULL_SENSITIVITY,
				impulse.y * settings.sensitivity.vLookSensitivity * DEGREES_PER_SECOND_AT_FULL_SENSITIVITY
		);
	}
}
