/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/// A category of feedback that the user can set the strength of, such as `controlify:world`.
/// Which events belong to which source is data: see [FeedbackSourceManager].
public record FeedbackSource(Identifier id) {
	public static final Codec<FeedbackSource> CODEC = Identifier.CODEC
			.xmap(FeedbackSource::new, FeedbackSource::id);

	/// The overall strength, which every other source is multiplied by. Events that no
	/// source lists are controlled by this alone.
	public static final FeedbackSource MASTER = new FeedbackSource(CUtil.rl("master"));

	public FeedbackSource {
		Objects.requireNonNull(id);
	}
}
