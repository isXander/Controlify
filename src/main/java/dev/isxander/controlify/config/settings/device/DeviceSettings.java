/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings.device;

import dev.isxander.controlify.config.dto.device.DeviceConfig;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DeviceSettings {
	public final GyroCalibrationSettings gyroCalibration;
	public @Nullable ControllerMapping mapping;

	private static final DeviceSettings DEFAULT = new DeviceSettings();

	private DeviceSettings() {
		this.gyroCalibration = GyroCalibrationSettings.defaults();
		this.mapping = null;
	}

	public DeviceSettings(
			GyroCalibrationSettings gyroCalibration,
			@Nullable ControllerMapping mapping
	) {
		this.gyroCalibration = gyroCalibration;
		this.mapping = mapping;
	}

	public static DeviceSettings defaults() {
		return DEFAULT;
	}

	public static DeviceSettings fromDTO(DeviceConfig dto) {
		return new DeviceSettings(
				GyroCalibrationSettings.fromDTO(dto.gyroCalibration()),
				dto.mapping().orElse(null)
		);
	}

	public DeviceConfig toDTO() {
		return new DeviceConfig(
				gyroCalibration.toDTO(),
				Optional.ofNullable(mapping)
		);
	}
}
