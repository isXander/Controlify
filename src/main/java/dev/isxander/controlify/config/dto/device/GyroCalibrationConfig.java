/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.gyro.GyroStateC;

public record GyroCalibrationConfig(
		GyroStateC offset
) {
	public static final Codec<GyroCalibrationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			GyroStateC.CODEC_MUTABLE.optionalFieldOf("offset", GyroStateC.ZERO).forGetter(GyroCalibrationConfig::offset)
	).apply(instance, GyroCalibrationConfig::new));
}
