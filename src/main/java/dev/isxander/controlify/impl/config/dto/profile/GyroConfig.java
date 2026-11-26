/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.controller.gyro.GyroActivation;
import dev.isxander.controlify.impl.controller.gyro.GyroSpace;
import net.minecraft.resources.Identifier;

import java.util.List;

public record GyroConfig(
		boolean enabled,
		GyroSpace space,
		float sensitivity,
		float verticalRatio,
		float fastSensitivity,
		float accelSlowThreshold,
		float accelFastThreshold,
		float smoothThreshold,
		float smoothTime,
		float tightenThreshold,
		GyroActivation activation,
		boolean activeWhileAiming,
		float aimMultiplier,
		List<Identifier> touchActivators,
		boolean invertPitch,
		boolean invertYaw
) {
	public static final Codec<GyroConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("enabled").forGetter(GyroConfig::enabled),
			GyroSpace.CODEC.fieldOf("space").forGetter(GyroConfig::space),
			Codec.FLOAT.fieldOf("sensitivity").forGetter(GyroConfig::sensitivity),
			Codec.FLOAT.fieldOf("vertical_ratio").forGetter(GyroConfig::verticalRatio),
			Codec.FLOAT.fieldOf("fast_sensitivity").forGetter(GyroConfig::fastSensitivity),
			Codec.FLOAT.fieldOf("accel_slow_threshold").forGetter(GyroConfig::accelSlowThreshold),
			Codec.FLOAT.fieldOf("accel_fast_threshold").forGetter(GyroConfig::accelFastThreshold),
			Codec.FLOAT.fieldOf("smooth_threshold").forGetter(GyroConfig::smoothThreshold),
			Codec.FLOAT.fieldOf("smooth_time").forGetter(GyroConfig::smoothTime),
			Codec.FLOAT.fieldOf("tighten_threshold").forGetter(GyroConfig::tightenThreshold),
			GyroActivation.CODEC.fieldOf("activation").forGetter(GyroConfig::activation),
			Codec.BOOL.fieldOf("active_while_aiming").forGetter(GyroConfig::activeWhileAiming),
			Codec.FLOAT.fieldOf("aim_multiplier").forGetter(GyroConfig::aimMultiplier),
			Identifier.CODEC.listOf().fieldOf("touch_activators").forGetter(GyroConfig::touchActivators),
			Codec.BOOL.fieldOf("invert_pitch").forGetter(GyroConfig::invertPitch),
			Codec.BOOL.fieldOf("invert_yaw").forGetter(GyroConfig::invertYaw)
	).apply(instance, GyroConfig::new));
}
