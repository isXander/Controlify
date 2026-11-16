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

/// When gyro look is active.
public enum GyroActivation implements StringRepresentable, NameableEnum {
	/// Always active.
	ALWAYS,
	/// Active while the gyro button is held or a touch activator is touched.
	HOLD_ON,
	/// Active unless the gyro button is held or a touch activator is touched.
	HOLD_OFF,
	/// The gyro button toggles it. Touch activators are ignored.
	TOGGLE;

	public static final Codec<GyroActivation> CODEC = StringRepresentable.fromEnum(GyroActivation::values);

	@Override
	public @NonNull String getSerializedName() {
		return this.name().toLowerCase();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("controlify.gyro_activation." + this.name().toLowerCase());
	}
}
