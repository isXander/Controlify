/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.profile;

import dev.isxander.controlify.impl.config.dto.profile.GyroConfig;
import dev.isxander.controlify.impl.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.impl.controller.gyro.GyroYawMode;

public class GyroSettings {
	public float lookSensitivity;
	public boolean relativeMode;
	public boolean invertPitch;
	public boolean invertYaw;
	public GyroButtonMode buttonMode;
	public GyroYawMode yawMode;
	public boolean flickStick;
	public float lowPassCutoffHz;

	public GyroSettings(
			float lookSensitivity,
			boolean relativeMode,
			boolean invertPitch,
			boolean invertYaw,
			GyroButtonMode buttonMode,
			GyroYawMode yawMode,
			boolean flickStick,
			float lowPassCutoffHz
	) {
		this.lookSensitivity = lookSensitivity;
		this.relativeMode = relativeMode;
		this.invertPitch = invertPitch;
		this.invertYaw = invertYaw;
		this.buttonMode = buttonMode;
		this.yawMode = yawMode;
		this.flickStick = flickStick;
		this.lowPassCutoffHz = lowPassCutoffHz;
	}

	public static GyroSettings fromDTO(GyroConfig dto) {
		return new GyroSettings(
				dto.lookSensitivity(),
				dto.relativeMode(),
				dto.invertPitch(),
				dto.invertYaw(),
				dto.buttonMode(),
				dto.yawMode(),
				dto.flickStick(),
				dto.lowPassCutoffHz()
		);
	}

	public GyroConfig toDTO() {
		return new GyroConfig(
				lookSensitivity,
				relativeMode,
				invertPitch,
				invertYaw,
				buttonMode,
				yawMode,
				flickStick,
				lowPassCutoffHz
		);
	}
}
