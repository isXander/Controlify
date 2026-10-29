/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.device;

import dev.isxander.controlify.impl.config.dto.device.GyroCalibrationConfig;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class GyroCalibrationSettings {
	public Vector3fc offset;

	private GyroCalibrationSettings() {
		this.offset = new Vector3f();
	}

	public GyroCalibrationSettings(
			Vector3fc offset
	) {
		this.offset = offset;
	}

	public static GyroCalibrationSettings defaults() {
		return new GyroCalibrationSettings();
	}

	public static GyroCalibrationSettings fromDTO(GyroCalibrationConfig dto) {
		return new GyroCalibrationSettings(
				dto.offset()
		);
	}

	public GyroCalibrationConfig toDTO() {
		return new GyroCalibrationConfig(
				offset
		);
	}
}
