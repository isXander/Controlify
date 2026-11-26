/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.impl.config.dto.dfu.ControlifyTypeReferences;

import java.util.Locale;
import java.util.stream.Stream;

/// The gyro settings were reshaped: `look_sensitivity` (0 = off), `button_mode`, `yaw_mode` and `flick_stick`
/// became `enabled`, `activation`, `space` and a separate top-level `flick_stick` section.
/// The values are fixed here so later settings changes do not alter this migration.
public final class GyroReworkFix extends DataFix {
	public GyroReworkFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: rework gyro and flick stick settings",
				getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG),
				typed -> typed.update(DSL.remainderFinder(), this::rewrite)
		);
	}

	private <T> Dynamic<T> rewrite(Dynamic<T> root) {
		Dynamic<T> old = root.get("gyro").orElseEmptyMap();
		if (old.get("look_sensitivity").result().isEmpty() && old.get("button_mode").result().isEmpty()
				&& old.get("yaw_mode").result().isEmpty() && old.get("flick_stick").result().isEmpty()) {
			// already the new shape (or nothing to migrate)
			return root;
		}

		float lookSensitivity = old.get("look_sensitivity").asFloat(0f);
		boolean enabled = lookSensitivity > 0f;
		float sensitivity = enabled ? lookSensitivity : 1f;

		String space = switch (old.get("yaw_mode").asString("yaw").toLowerCase(Locale.ROOT)) {
			case "roll" -> "local_roll";
			case "both" -> "local_both";
			default -> "local_yaw";
		};
		// the old "off" button mode had no condition at all, so gyro was always active
		String activation = switch (old.get("button_mode").asString("on").toLowerCase(Locale.ROOT)) {
			case "invert" -> "hold_off";
			case "toggle" -> "toggle";
			case "off" -> "always";
			default -> "hold_on";
		};

		Dynamic<T> gyro = root.emptyMap()
				.set("enabled", root.createBoolean(enabled))
				.set("space", root.createString(space))
				.set("sensitivity", root.createFloat(sensitivity))
				.set("vertical_ratio", root.createFloat(1f))
				// no acceleration: both sensitivities match, as the old gyro had a single flat sensitivity
				.set("fast_sensitivity", root.createFloat(sensitivity))
				.set("accel_slow_threshold", root.createFloat(0f))
				.set("accel_fast_threshold", root.createFloat(0f))
				.set("smooth_threshold", root.createFloat(0f))
				.set("smooth_time", root.createFloat(0.1f))
				.set("tighten_threshold", root.createFloat(0f))
				.set("activation", root.createString(activation))
				// the old gyro always worked while aiming, regardless of the button
				.set("active_while_aiming", root.createBoolean(true))
				.set("aim_multiplier", root.createFloat(1f))
				.set("touch_activators", root.createList(Stream.empty()))
				.set("invert_pitch", root.createBoolean(old.get("invert_pitch").asBoolean(false)))
				.set("invert_yaw", root.createBoolean(old.get("invert_yaw").asBoolean(false)));

		root = root.set("gyro", gyro);

		if (root.get("flick_stick").result().isEmpty()) {
			// the old flick stick only worked with gyro on, and replaced regular stick look entirely
			Dynamic<T> flickStick = root.emptyMap()
					.set("enabled", root.createBoolean(enabled && old.get("flick_stick").asBoolean(false)))
					.set("flick_time", root.createFloat(0.4f))
					.set("forward_deadzone", root.createFloat(0f))
					.set("snap", root.createString("none"))
					.set("rotate", root.createBoolean(false));
			root = root.set("flick_stick", flickStick);
		}

		return root;
	}
}
