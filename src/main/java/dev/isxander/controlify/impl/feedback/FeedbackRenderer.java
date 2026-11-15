/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

/// Turns the engine's mixed output into something one kind of hardware output can reproduce.
public interface FeedbackRenderer {
	/// Renders nothing.
	FeedbackRenderer NONE = new FeedbackRenderer() {
		@Override
		public void render(MixedFrame frame, float dt) {
		}

		@Override
		public void silence() {
		}
	};

	/// Called once per rendered frame with the mixed output, including frames where nothing plays.
	///
	/// @param dt seconds since the previous frame
	void render(MixedFrame frame, float dt);

	/// Turns this renderer's output off. May be called every frame; implementations must
	/// avoid redundant hardware writes.
	void silence();
}
