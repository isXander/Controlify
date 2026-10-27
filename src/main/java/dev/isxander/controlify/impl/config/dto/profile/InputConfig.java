/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.ingame.InputCurves;
import dev.isxander.controlify.api.input.LogicalInput;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record InputConfig(
		BindingsConfig bindings,
		SensitivityConfig sensitivity,
		RadialMenuConfig radialMenu,
		GestureConfig gestures,
		float buttonActivationThreshold,
		boolean keepDefaultBindings
) {
	public static final Codec<InputConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BindingsConfig.CODEC.optionalFieldOf("bindings", BindingsConfig.EMPTY).forGetter(InputConfig::bindings),
			SensitivityConfig.CODEC.fieldOf("sensitivity").forGetter(InputConfig::sensitivity),
			RadialMenuConfig.CODEC.fieldOf("radial_menu").forGetter(InputConfig::radialMenu),
			GestureConfig.CODEC.fieldOf("gestures").forGetter(InputConfig::gestures),
			Codec.floatRange(0.01f, 1f).fieldOf("button_activation_threshold").forGetter(InputConfig::buttonActivationThreshold),
			Codec.BOOL.fieldOf("keep_default_bindings").forGetter(InputConfig::keepDefaultBindings)
	).apply(instance, InputConfig::new));

	public record BindingsConfig(
			Map<Identifier, Optional<LogicalInput>> bindings
	) {
		public static final BindingsConfig EMPTY = new BindingsConfig(Map.of());

		public static final Codec<BindingsConfig> CODEC = Codec.unboundedMap(Identifier.CODEC, LogicalInput.OPTIONAL_CODEC)
				.xmap(BindingsConfig::new, BindingsConfig::bindings);

		public BindingsConfig {
			bindings = Map.copyOf(bindings);
		}
	}

	public record SensitivityConfig(
		float hLookSensitivity,
		float vLookSensitivity,
		boolean hLookInvert,
		boolean vLookInvert,
		float virtualMouseSensitivity,
		boolean reduceAimingSensitivity,
		InputCurves lookInputCurve,
		boolean isLCE,
		float defaultDeadzone,
		Map<Identifier, Float> deadzones
	) {
		public static final Codec<SensitivityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("horizontal").forGetter(SensitivityConfig::hLookSensitivity),
				Codec.FLOAT.fieldOf("vertical").forGetter(SensitivityConfig::vLookSensitivity),
				Codec.BOOL.fieldOf("horizontal_invert").forGetter(SensitivityConfig::hLookInvert),
				Codec.BOOL.fieldOf("vertical_invert").forGetter(SensitivityConfig::vLookInvert),
				Codec.FLOAT.fieldOf("virtual_mouse").forGetter(SensitivityConfig::virtualMouseSensitivity),
				Codec.BOOL.fieldOf("reduce_aiming").forGetter(SensitivityConfig::reduceAimingSensitivity),
				InputCurves.CODEC.fieldOf("look_curve").forGetter(SensitivityConfig::lookInputCurve),
				Codec.BOOL.fieldOf("is_lce").forGetter(SensitivityConfig::isLCE),
				Codec.floatRange(0f, 1f).fieldOf("default_deadzone").forGetter(SensitivityConfig::defaultDeadzone),
				Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).optionalFieldOf("deadzones", Map.of()).forGetter(SensitivityConfig::deadzones)
		).apply(instance, SensitivityConfig::new));
	}

	/// Button gesture timings in milliseconds.
	/// @param tapMaxMs the longest press that still counts as a tap
	/// @param doubleTapGapMs the longest gap between releasing a tap and pressing again for a double tap
	/// @param holdMs how long a press lasts before it counts as held; raised above tapMaxMs if needed,
	///               since the settings screen sets each value independently
	public record GestureConfig(
			int tapMaxMs,
			int doubleTapGapMs,
			int holdMs
	) {
		public static final Codec<GestureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.intRange(0, 2000).fieldOf("tap_max_ms").forGetter(GestureConfig::tapMaxMs),
				Codec.intRange(0, 2000).fieldOf("double_tap_gap_ms").forGetter(GestureConfig::doubleTapGapMs),
				Codec.intRange(1, 5000).fieldOf("hold_ms").forGetter(GestureConfig::holdMs)
		).apply(instance, GestureConfig::new));

		public GestureConfig {
			holdMs = Math.max(holdMs, tapMaxMs + 1);
		}
	}

	public record RadialMenuConfig(
			List<Identifier> radialActions,
			int radialButtonFocusTimeoutTicks
	) {
		public static final Codec<RadialMenuConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.listOf().fieldOf("actions").forGetter(RadialMenuConfig::radialActions),
				Codec.INT.fieldOf("button_focus_timeout_ticks").forGetter(RadialMenuConfig::radialButtonFocusTimeoutTicks)
		).apply(instance, RadialMenuConfig::new));
	}
}
