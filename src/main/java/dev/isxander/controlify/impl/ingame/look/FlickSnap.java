/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import com.mojang.serialization.Codec;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

/// How a flick stick angle is snapped to fixed steps.
public enum FlickSnap implements StringRepresentable, NameableEnum {
	/// The flick goes exactly where the stick points.
	NONE(0),
	/// Snap to 90 degree steps.
	FOUR(90),
	/// Snap to 45 degree steps.
	EIGHT(45);

	public static final Codec<FlickSnap> CODEC = StringRepresentable.fromEnum(FlickSnap::values);

	private final float stepDegrees;

	FlickSnap(float stepDegrees) {
		this.stepDegrees = stepDegrees;
	}

	/// @return the step in degrees, or 0 when there is no snapping
	public float stepDegrees() {
		return this.stepDegrees;
	}

	@Override
	public @NonNull String getSerializedName() {
		return this.name().toLowerCase();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("controlify.flick_snap." + this.name().toLowerCase());
	}
}
