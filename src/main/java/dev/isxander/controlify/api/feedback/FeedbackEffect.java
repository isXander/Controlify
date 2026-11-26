/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

/// A reusable, stateless effect definition ("what"). All playback state lives in the
/// [Playback] the engine passes in, so one definition may play many times at once.
///
/// @param <P> the params type of the event this effect plays for
@FunctionalInterface
public interface FeedbackEffect<P> {
	/// Writes this effect's output at the playback's current time into `out`.
	///
	/// @return false when the effect is finished; anything written to `out` in that call is
	///         ignored. An effect for a held event should return false once
	///         [Playback#released()] is true, or after a release tail.
	boolean sample(Playback<P> playback, FeedbackFrame out);
}
