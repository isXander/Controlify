/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import org.jetbrains.annotations.ApiStatus;

/// The state of one running playback of a [FeedbackEffect], owned by the engine.
/// Playbacks are sampled once per rendered frame, not once per game tick.
///
/// @param <P> the params type of the event the effect is playing for
@ApiStatus.NonExtendable
public interface Playback<P> {
	/// Seconds since the playback started. Zero on the first sample.
	float seconds();

	/// [#seconds] of the previous sample, or -1 on the first sample.
	float previousSeconds();

	/// The params the emit site passed. For held events these are the latest ones.
	/// Null for events without params.
	P params();

	/// A named float the event declared with `FeedbackEvent.Builder#param`, as data-driven
	/// effects read them.
	///
	/// @return the value, or `fallback` if the event declares no such param
	float param(String name, float fallback);

	default float param(String name) {
		return this.param(name, 0f);
	}

	/// Whether this playback was started by `hold` (and so ends on release) rather than `fire`.
	boolean held();

	/// Seconds since the playback was released, or -1 if it has not been.
	float sinceRelease();

	default boolean released() {
		return this.sinceRelease() >= 0f;
	}

	/// Whether the playback passed `time` on this sample. True for exactly one sample, so it
	/// is the way to trigger a [FeedbackFrame#click].
	default boolean crossed(float time) {
		return this.previousSeconds() < time && time <= this.seconds();
	}
}
