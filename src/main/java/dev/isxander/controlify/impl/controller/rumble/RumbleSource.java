/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/// A source identifier, with no registration or availability implied by construction.
/// Available sources are determined by RumbleSourceManager's resource-pack assignments.
public record RumbleSource(Identifier id) {
	public static final Codec<RumbleSource> CODEC = Identifier.CODEC
			.xmap(RumbleSource::new, RumbleSource::id);

	/// The global gain/fallback source; this constant does not declare its availability.
	public static final RumbleSource MASTER = new RumbleSource(CUtil.rl("master"));

	public RumbleSource {
		Objects.requireNonNull(id);
	}
}
