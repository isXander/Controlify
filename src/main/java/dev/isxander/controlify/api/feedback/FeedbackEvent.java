/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import dev.isxander.controlify.impl.feedback.FeedbackEventImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Something that happens in the game that the player should feel ("when").
///
/// Emit sites hold no playback state: they call [#fire] once for a one-shot, or [#hold]
/// every tick while a condition is true, and the engine of the current controller decides
/// which [FeedbackEffect]s play. Events target the current controller.
///
/// An event plays the effect with its own id, unless feedback rules
/// (`controller/contextual/feedback/*.json`) name it: then it plays the effect of every rule
/// whose predicate matches.
///
/// Params passed as `null` are treated as all zero, so parameterless events can
/// use [#fire()] and [#hold()].
///
/// @param <P> the type the emit site passes its game-derived numbers in
@ApiStatus.NonExtendable
public interface FeedbackEvent<P> {
	Identifier id();

	/// One-shot. The effects to play are chosen once; the playbacks are then committed.
	void fire(@Nullable P params);

	/// One-shot with central distance falloff from the camera to `pos`.
	void fireAt(Vec3 pos, @Nullable P params);

	/// Call every tick while the condition is true. Refreshes the params and chooses the
	/// effects again: one that is no longer chosen is released, and a new one is started.
	/// Everything is released when calls stop for a short time.
	void hold(@Nullable P params);

	/// [#hold] with central distance falloff from the camera to `pos`.
	void holdAt(Vec3 pos, @Nullable P params);

	/// Optionally ends a hold immediately instead of waiting for it to time out.
	void release();

	default void fire() {
		this.fire(null);
	}

	default void hold() {
		this.hold(null);
	}

	/// Starts building an event. Declaring a [Builder#param] fixes the params type.
	static Builder builder(Identifier id) {
		return FeedbackEventImpl.builder(id);
	}

	/// Extracts one named float from the emit site's params.
	@FunctionalInterface
	@ApiStatus.NonExtendable
	interface ParamSource<P> {
		float get(P params);
	}

	/// A builder for an event with no params declared yet.
	@ApiStatus.NonExtendable
	interface Builder {
		<P> TypedBuilder<P> param(String name, ParamSource<P> source);

		/// The range in blocks of central falloff for `fireAt` and `holdAt`. Default 32.
		Builder falloffRange(float blocks);

		FeedbackEvent<Void> build();
	}

	@ApiStatus.NonExtendable
	interface TypedBuilder<P> {
		TypedBuilder<P> param(String name, ParamSource<P> source);

		TypedBuilder<P> falloffRange(float blocks);

		FeedbackEvent<P> build();
	}
}
