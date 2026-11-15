/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.contextual.ContextualStateSink;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/// Everything the engine needs from the game, so it can run without a Minecraft client.
public interface FeedbackServices {
	/// The user's strength multiplier for a playback.
	///
	/// @param eventId  the event (or the id of a directly played effect)
	/// @param effectId the effect actually playing
	/// @param fallback the source to use if no resource pack assigns one
	float gain(Identifier eventId, Identifier effectId, FeedbackSource fallback);

	/// Whether the user has vibration enabled. If not, all renderers are silenced.
	boolean enabled();

	/// Whether an HD renderer is currently in use, reported to effects as `FeedbackFrame#hd()`.
	boolean hd();

	/// Central distance falloff, 0 to 1, or a negative value if there is no camera.
	float falloff(Vec3 pos, float range);

	/// Evaluates the event's selection rules.
	///
	/// @return the effect id of the first matching rule, or null if no rule matched
	@Nullable Identifier select(FeedbackEventImpl<?> event, @Nullable Consumer<ContextualStateSink> subject);

	/// Finds the effect with this id that can play for `eventId`: a data effect, else a code
	/// effect registered for that event.
	@Nullable FeedbackEffect<Object> findEffect(Identifier effectId, Identifier eventId);
}
