/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record DeviceConfig(
	String name,
	long lastSeen,
	Identifier controllerType,
	GyroCalibrationConfig gyroCalibration,
	Optional<ControllerMapping> mapping
) {
	public static final Codec<DeviceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("name", "").forGetter(DeviceConfig::name),
			Codec.LONG.optionalFieldOf("last_seen", 0L).forGetter(DeviceConfig::lastSeen),
			Identifier.CODEC.optionalFieldOf("controller_type", ControllerType.DEFAULT.namespace()).forGetter(DeviceConfig::controllerType),
			GyroCalibrationConfig.CODEC.fieldOf("gyro_calibration").forGetter(DeviceConfig::gyroCalibration),
			ControllerMapping.CODEC.optionalFieldOf("mapping").forGetter(DeviceConfig::mapping)
	).apply(instance, DeviceConfig::new));
}
