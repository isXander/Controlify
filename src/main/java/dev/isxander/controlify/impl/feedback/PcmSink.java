/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

/// A stereo audio output that drives a controller's left and right haptic actuators.
/// Implemented by drivers; written to by [PcmRenderer].
public interface PcmSink {
	/// Sample frames per second.
	int sampleRate();

	/// How many sample frames have been written but not yet played.
	int queuedFrames();

	/// Queues audio for playback.
	///
	/// @param samples interleaved left/right samples in the range -1 to 1
	/// @param frames  how many sample frames (pairs) of `samples` to write
	void write(float[] samples, int frames);
}
