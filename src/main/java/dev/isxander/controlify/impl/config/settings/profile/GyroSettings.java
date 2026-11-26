/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.profile;

import dev.isxander.controlify.impl.config.dto.profile.GyroConfig;
import dev.isxander.controlify.impl.controller.gyro.GyroActivation;
import dev.isxander.controlify.impl.controller.gyro.GyroSpace;
import dev.isxander.controlify.impl.input.pipeline.MotionParams;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GyroSettings {
	public boolean enabled;
	public GyroSpace space;
	public float sensitivity;
	public float verticalRatio;
	public float fastSensitivity;
	public float accelSlowThreshold;
	public float accelFastThreshold;
	public float smoothThreshold;
	public float smoothTime;
	public float tightenThreshold;
	public GyroActivation activation;
	public boolean activeWhileAiming;
	public float aimMultiplier;
	public Set<Identifier> touchActivators;
	public boolean invertPitch;
	public boolean invertYaw;

	public GyroSettings(
			boolean enabled,
			GyroSpace space,
			float sensitivity,
			float verticalRatio,
			float fastSensitivity,
			float accelSlowThreshold,
			float accelFastThreshold,
			float smoothThreshold,
			float smoothTime,
			float tightenThreshold,
			GyroActivation activation,
			boolean activeWhileAiming,
			float aimMultiplier,
			Set<Identifier> touchActivators,
			boolean invertPitch,
			boolean invertYaw
	) {
		this.enabled = enabled;
		this.space = space;
		this.sensitivity = sensitivity;
		this.verticalRatio = verticalRatio;
		this.fastSensitivity = fastSensitivity;
		this.accelSlowThreshold = accelSlowThreshold;
		this.accelFastThreshold = accelFastThreshold;
		this.smoothThreshold = smoothThreshold;
		this.smoothTime = smoothTime;
		this.tightenThreshold = tightenThreshold;
		this.activation = activation;
		this.activeWhileAiming = activeWhileAiming;
		this.aimMultiplier = aimMultiplier;
		this.touchActivators = touchActivators;
		this.invertPitch = invertPitch;
		this.invertYaw = invertYaw;
	}

	/// The motion processing parameters these settings describe.
	public MotionParams toMotionParams() {
		return new MotionParams(
				space, sensitivity, verticalRatio, fastSensitivity,
				accelSlowThreshold, accelFastThreshold, smoothThreshold, smoothTime, tightenThreshold
		);
	}

	public static GyroSettings fromDTO(GyroConfig dto) {
		return new GyroSettings(
				dto.enabled(),
				dto.space(),
				dto.sensitivity(),
				dto.verticalRatio(),
				dto.fastSensitivity(),
				dto.accelSlowThreshold(),
				dto.accelFastThreshold(),
				dto.smoothThreshold(),
				dto.smoothTime(),
				dto.tightenThreshold(),
				dto.activation(),
				dto.activeWhileAiming(),
				dto.aimMultiplier(),
				new LinkedHashSet<>(dto.touchActivators()),
				dto.invertPitch(),
				dto.invertYaw()
		);
	}

	public GyroConfig toDTO() {
		return new GyroConfig(
				enabled,
				space,
				sensitivity,
				verticalRatio,
				fastSensitivity,
				accelSlowThreshold,
				accelFastThreshold,
				smoothThreshold,
				smoothTime,
				tightenThreshold,
				activation,
				activeWhileAiming,
				aimMultiplier,
				List.copyOf(touchActivators),
				invertPitch,
				invertYaw
		);
	}
}
