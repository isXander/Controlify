/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record ProfileConfig(
		Optional<String> name,
		Optional<String> controllerUid,
		GenericControllerConfig generic,
		InputConfig input,
		RumbleConfig rumble,
		HDHapticConfig hdHaptic,
		GyroConfig gyro,
		BluetoothDeviceConfig bluetoothDevice,
		DualSenseConfig dualsense
) {
	public static final Codec<ProfileConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("name").forGetter(ProfileConfig::name),
			Codec.STRING.optionalFieldOf("controller_uid").forGetter(ProfileConfig::controllerUid),
			GenericControllerConfig.CODEC.fieldOf("generic").forGetter(ProfileConfig::generic),
			InputConfig.CODEC.fieldOf("input").forGetter(ProfileConfig::input),
			RumbleConfig.CODEC.fieldOf("rumble").forGetter(ProfileConfig::rumble),
			HDHapticConfig.CODEC.fieldOf("hd_haptic").forGetter(ProfileConfig::hdHaptic),
			GyroConfig.CODEC.fieldOf("gyro").forGetter(ProfileConfig::gyro),
			BluetoothDeviceConfig.CODEC.fieldOf("bluetooth_device").forGetter(ProfileConfig::bluetoothDevice),
			DualSenseConfig.CODEC.fieldOf("dualsense").forGetter(ProfileConfig::dualsense)
	).apply(instance, ProfileConfig::new));
}
