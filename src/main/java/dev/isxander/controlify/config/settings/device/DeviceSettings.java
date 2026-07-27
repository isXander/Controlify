/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings.device;

import dev.isxander.controlify.config.dto.device.DeviceConfig;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DeviceSettings {
	public String name;
	public long lastSeen;
	public Identifier controllerType;
	public final GyroCalibrationSettings gyroCalibration;
	public @Nullable ControllerMapping mapping;

	private DeviceSettings(String uid) {
		this.name = uid;
		this.lastSeen = 0L;
		this.controllerType = ControllerType.DEFAULT.namespace();
		this.gyroCalibration = GyroCalibrationSettings.defaults();
		this.mapping = null;
	}

	public DeviceSettings(
			String name,
			long lastSeen,
			Identifier controllerType,
			GyroCalibrationSettings gyroCalibration,
			@Nullable ControllerMapping mapping
	) {
		this.name = name;
		this.lastSeen = lastSeen;
		this.controllerType = controllerType;
		this.gyroCalibration = gyroCalibration;
		this.mapping = mapping;
	}

	public static DeviceSettings defaults(String uid) {
		return new DeviceSettings(uid);
	}

	public static DeviceSettings fromDTO(DeviceConfig dto) {
		return new DeviceSettings(
				dto.name(),
				dto.lastSeen(),
				dto.controllerType(),
				GyroCalibrationSettings.fromDTO(dto.gyroCalibration()),
				dto.mapping().orElse(null)
		);
	}

	public DeviceConfig toDTO() {
		return new DeviceConfig(
				name,
				lastSeen,
				controllerType,
				gyroCalibration.toDTO(),
				Optional.ofNullable(mapping)
		);
	}
}
