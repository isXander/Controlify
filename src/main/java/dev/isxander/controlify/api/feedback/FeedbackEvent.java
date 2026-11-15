/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import dev.isxander.controlify.api.contextual.ContextualStateSink;
import dev.isxander.controlify.impl.feedback.FeedbackEventImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/// Something that happens in the game that the player should feel ("when").
///
/// Emit sites hold no playback state: they call [#fire] once for a one-shot, or [#hold]
/// every tick while a condition is true, and the engine of the current controller decides
/// which [FeedbackEffect] plays. Events target the current controller.
///
/// Params passed as `null` are treated as all zero, so parameterless events can
/// use [#fire()] and [#hold()].
///
/// The optional `subject` consumer contributes extra contextual state (for example the
/// item or block the event is about) that is visible to selection rules for this call only.
///
/// @param <P> the type the emit site passes its game-derived numbers in
public interface FeedbackEvent<P> {
	Identifier id();

	/// One-shot. Selection is evaluated once; the playback is then committed.
	void fire(@Nullable P params, @Nullable Consumer<ContextualStateSink> subject);

	/// One-shot with central distance falloff from the camera to `pos`.
	void fireAt(Vec3 pos, @Nullable P params, @Nullable Consumer<ContextualStateSink> subject);

	/// Call every tick while the condition is true. Refreshes the playback's params and
	/// re-evaluates selection; if the selected effect changed, the old playback is released
	/// and the new one started. The playback is released when calls stop for a short time.
	void hold(@Nullable P params, @Nullable Consumer<ContextualStateSink> subject);

	/// [#hold] with central distance falloff from the camera to `pos`.
	void holdAt(Vec3 pos, @Nullable P params, @Nullable Consumer<ContextualStateSink> subject);

	/// Optionally ends a hold immediately instead of waiting for it to time out.
	void release();

	default void fire() {
		this.fire(null, null);
	}

	default void fire(@Nullable P params) {
		this.fire(params, null);
	}

	default void fireAt(Vec3 pos, @Nullable P params) {
		this.fireAt(pos, params, null);
	}

	default void hold() {
		this.hold(null, null);
	}

	default void hold(@Nullable P params) {
		this.hold(params, null);
	}

	default void holdAt(Vec3 pos, @Nullable P params) {
		this.holdAt(pos, params, null);
	}

	/// Starts building an event. Declaring a [Builder#param] fixes the params type.
	static Builder builder(Identifier id) {
		return FeedbackEventImpl.builder(id);
	}

	/// Extracts one named float from the emit site's params.
	@FunctionalInterface
	interface ParamSource<P> {
		float get(P params);
	}

	/// A builder for an event with no params declared yet.
	interface Builder {
		<P> TypedBuilder<P> param(String name, ParamSource<P> source);

		/// Marks this event as using selection rules (`controller/contextual/feedback/*.json`).
		Builder selection();

		/// The range in blocks of central falloff for `fireAt` and `holdAt`. Default 32.
		Builder falloffRange(float blocks);

		FeedbackEvent<Void> build();
	}

	interface TypedBuilder<P> {
		TypedBuilder<P> param(String name, ParamSource<P> source);

		TypedBuilder<P> selection();

		TypedBuilder<P> falloffRange(float blocks);

		FeedbackEvent<P> build();
	}
}
