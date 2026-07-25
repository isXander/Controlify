/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.config.dto.device.DeviceConfig;

import java.util.Map;

public record SharedConfig(
		GlobalConfig globalConfig,
		Map<String, DeviceConfig> deviceConfig
) {
	public static final Codec<SharedConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			GlobalConfig.CODEC.fieldOf("global").forGetter(SharedConfig::globalConfig),
			Codec.unboundedMap(Codec.STRING, DeviceConfig.CODEC).fieldOf("devices").forGetter(SharedConfig::deviceConfig)
	).apply(instance, SharedConfig::new));
}
