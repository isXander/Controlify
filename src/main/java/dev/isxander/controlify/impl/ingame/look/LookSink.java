/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

/// Applies the final rotation for the frame, in degrees.
@FunctionalInterface
public interface LookSink {
	void apply(double yawDeg, double pitchDeg);
}
