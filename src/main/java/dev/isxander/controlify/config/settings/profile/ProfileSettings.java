/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ProfileSettings {
	public @Nullable String name;
	public @Nullable String controllerUid;
	public final GenericControllerSettings generic;
	public final InputSettings input;
	public final RumbleSettings rumble;
	public final HDHapticSettings hdHaptic;
	public final GyroSettings gyro;
	public final BluetoothDeviceSettings bluetoothDevice;

	public ProfileSettings(
			@Nullable String name,
			@Nullable String controllerUid,
			GenericControllerSettings generic,
			InputSettings input,
			RumbleSettings rumble,
			HDHapticSettings hdHaptic,
			GyroSettings gyro,
			BluetoothDeviceSettings bluetoothDevice
	) {
		this.name = name;
		this.controllerUid = controllerUid;
		this.generic = generic;
		this.input = input;
		this.rumble = rumble;
		this.hdHaptic = hdHaptic;
		this.gyro = gyro;
		this.bluetoothDevice = bluetoothDevice;
	}

	public static ProfileSettings fromDTO(ProfileConfig dto) {
		return new ProfileSettings(
				dto.name().orElse(null),
				dto.controllerUid().orElse(null),
				GenericControllerSettings.fromDTO(dto.generic()),
				InputSettings.fromDTO(dto.input()),
				RumbleSettings.fromDTO(dto.rumble()),
				HDHapticSettings.fromDTO(dto.hdHaptic()),
				GyroSettings.fromDTO(dto.gyro()),
				BluetoothDeviceSettings.fromDTO(dto.bluetoothDevice())
		);
	}

	public ProfileConfig toDTO() {
		return new ProfileConfig(
				Optional.ofNullable(name),
				Optional.ofNullable(controllerUid),
				generic.toDTO(),
				input.toDTO(),
				rumble.toDTO(),
				hdHaptic.toDTO(),
				gyro.toDTO(),
				bluetoothDevice.toDTO()
		);
	}

	public static ProfileSettings createDefault() {
		var dto = Controlify.instance()
				.defaultConfigManager()
				.getDefault();
		return ProfileSettings.fromDTO(dto);
	}
}
