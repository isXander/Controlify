/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.gyro;

import com.mojang.serialization.Codec;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

/// How controller rotation is turned into yaw and pitch.
public enum GyroSpace implements StringRepresentable, NameableEnum {
	/// Yaw from the controller's Y axis.
	LOCAL_YAW,
	/// Yaw from the controller's Z axis.
	LOCAL_ROLL,
	/// Yaw from both the Y and Z axes.
	LOCAL_BOTH,
	/// Yaw is rotation about the real-world vertical; pitch is about the horizontal axis nearest the controller's X axis.
	/// Behaves like {@link #LOCAL_YAW} until a gravity estimate exists.
	WORLD,
	/// Yaw follows the world vertical but is capped by the controller's own Y/Z rotation. Pitch is local.
	/// Behaves like {@link #LOCAL_YAW} until a gravity estimate exists.
	PLAYER;

	public static final Codec<GyroSpace> CODEC = StringRepresentable.fromEnum(GyroSpace::values);

	@Override
	public @NonNull String getSerializedName() {
		return this.name().toLowerCase();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("controlify.gyro_space." + this.name().toLowerCase());
	}
}
