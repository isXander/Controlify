/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.impl.controller.gyro.GyroYawMode;

public record GyroConfig(
		float lookSensitivity,
		boolean relativeMode,
		boolean invertPitch,
		boolean invertYaw,
		GyroButtonMode buttonMode,
		GyroYawMode yawMode,
		boolean flickStick,
		float lowPassCutoffHz
) {
	public static final Codec<GyroConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("look_sensitivity").forGetter(GyroConfig::lookSensitivity),
			Codec.BOOL.fieldOf("relative_mode").forGetter(GyroConfig::relativeMode),
			Codec.BOOL.fieldOf("invert_pitch").forGetter(GyroConfig::invertPitch),
			Codec.BOOL.fieldOf("invert_yaw").forGetter(GyroConfig::invertYaw),
			GyroButtonMode.CODEC.fieldOf("button_mode").forGetter(GyroConfig::buttonMode),
			GyroYawMode.CODEC.fieldOf("yaw_mode").forGetter(GyroConfig::yawMode),
			Codec.BOOL.fieldOf("flick_stick").forGetter(GyroConfig::flickStick),
			Codec.FLOAT
					.validate(f -> Float.isFinite(f) && f >= 0
							? DataResult.success(f)
							: DataResult.error(() -> "low pass cutoff hz must be finite and positive"))
					.fieldOf("low_pass_cutoff_hz")
					.forGetter(GyroConfig::lowPassCutoffHz)
	).apply(instance, GyroConfig::new));

}
