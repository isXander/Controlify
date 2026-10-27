/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

/// Button gesture timings used by {@link PhysicalToLogicalStage}.
/// @param tapMaxNanos the longest press that still counts as a tap
/// @param doubleTapGapNanos the longest gap between releasing a tap and pressing again for a double tap
/// @param holdNanos how long a press lasts before it counts as held; always greater than tapMaxNanos
public record GestureTimings(long tapMaxNanos, long doubleTapGapNanos, long holdNanos) {
	public static final GestureTimings DEFAULT = ofMillis(200, 200, 400);

	public GestureTimings {
		if (tapMaxNanos < 0 || doubleTapGapNanos < 0 || holdNanos <= tapMaxNanos) {
			throw new IllegalArgumentException("Gesture durations must be nonnegative and hold must exceed tap");
		}
	}

	/// Raises the hold duration above the tap duration if needed, so unvalidated settings never throw.
	public static GestureTimings ofMillis(int tapMaxMs, int doubleTapGapMs, int holdMs) {
		int tapMax = Math.max(0, tapMaxMs);
		return new GestureTimings(
				tapMax * 1_000_000L,
				Math.max(0, doubleTapGapMs) * 1_000_000L,
				Math.max(holdMs, tapMax + 1) * 1_000_000L
		);
	}
}
