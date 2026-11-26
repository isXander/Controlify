/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

import org.jetbrains.annotations.ApiStatus;

/// Where a [LookScaler] multiplies the look contributions for the frame.
@ApiStatus.NonExtendable
public interface LookScales {
	/// Multiplies rate contributions.
	void rate(double yaw, double pitch);

	/// Multiplies direct contributions.
	void direct(double yaw, double pitch);
}
