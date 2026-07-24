/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.rumble;

public interface RumbleCapable {
	boolean setRumble(float strongMagnitude, float weakMagnitude);

	boolean supportsRumble();

	RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source);
}
