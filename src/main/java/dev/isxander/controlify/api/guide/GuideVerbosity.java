/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

import com.mojang.serialization.Codec;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum GuideVerbosity implements NameableEnum, StringRepresentable {
	FULL(3),
	REDUCED(2),
	MINIMAL(1);

	public static final Codec<GuideVerbosity> CODEC = StringRepresentable.fromEnum(GuideVerbosity::values);

	private final int level;

	GuideVerbosity(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("controlify.guide_verbosity." + name().toLowerCase());
	}

	@Override
	public @NonNull String getSerializedName() {
		return name().toLowerCase();
	}
}
