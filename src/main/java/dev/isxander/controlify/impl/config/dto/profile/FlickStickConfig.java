/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.ingame.look.FlickSnap;

public record FlickStickConfig(
		boolean enabled,
		float flickTime,
		float forwardDeadzone,
		FlickSnap snap,
		boolean rotate
) {
	public static final Codec<FlickStickConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("enabled").forGetter(FlickStickConfig::enabled),
			Codec.FLOAT.fieldOf("flick_time").forGetter(FlickStickConfig::flickTime),
			Codec.FLOAT.fieldOf("forward_deadzone").forGetter(FlickStickConfig::forwardDeadzone),
			FlickSnap.CODEC.fieldOf("snap").forGetter(FlickStickConfig::snap),
			Codec.BOOL.fieldOf("rotate").forGetter(FlickStickConfig::rotate)
	).apply(instance, FlickStickConfig::new));
}
