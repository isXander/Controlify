/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record GyroCalibrationConfig(
		Vector3fc offset
) {
	public static final Codec<GyroCalibrationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.VECTOR3F.optionalFieldOf("offset", new Vector3f()).forGetter(GyroCalibrationConfig::offset)
	).apply(instance, GyroCalibrationConfig::new));
}
