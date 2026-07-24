/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.gyro;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum GyroYawMode implements StringRepresentable {
	YAW,
	ROLL,
	BOTH;

	public static final Codec<GyroYawMode> CODEC = StringRepresentable.fromEnum(GyroYawMode::values);

	@Override
	public @NonNull String getSerializedName() {
		return this.name().toLowerCase();
	}
}
