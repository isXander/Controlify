/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.FeedbackEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Everything the engine needs from the game, so it can run without a Minecraft client.
public interface FeedbackServices {
	/// How a playback is mixed, from the groups it belongs to.
	///
	/// @param effectId the effect playing
	/// @param fallback the group to use if no resource pack assigns one
	Mixing mixing(Identifier effectId, FeedbackGroup fallback);

	/// @param gain the user's strength multiplier
	/// @param rank the rank of the playback's groups; higher ranks draw on top
	record Mixing(float gain, int rank) {
	}

	/// Whether the effect is still felt while the game is paused, as feedback for menus must be.
	/// Everything else is silenced by [FeedbackEngine#setGameSilent].
	///
	/// @param effectId the effect playing
	/// @param fallback the group to use if no resource pack assigns one
	default boolean feltWhilePaused(Identifier effectId, FeedbackGroup fallback) {
		return false;
	}

	/// Whether the user has vibration enabled. If not, all renderers are silenced.
	boolean enabled();

	/// Whether an HD renderer is currently in use, reported to effects as `FeedbackFrame#hd()`.
	boolean hd();

	/// Central distance falloff, 0 to 1, or a negative value if there is no camera.
	float falloff(Vec3 pos, float range);

	/// The ids of the effects the event plays right now: one for every rule that matches, or
	/// the event's own id if none do. See [FeedbackSelection#select].
	List<Identifier> select(FeedbackEventImpl<?> event);

	/// Finds the effect with this id that can play for `eventId`: a data effect, else a code
	/// effect registered for that event.
	@Nullable FeedbackEffect<Object> findEffect(Identifier effectId, Identifier eventId);
}
