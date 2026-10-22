/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.gyro;

import org.joml.Vector3fc;

/// Gyroscope input. Presence in Controller.gyro() indicates gyro support.
public interface GyroComponent {
	/// The latest calibrated, filtered and integrated gyro interval in radians.
	/// Returns zero when the latest interval is older than 50ms. Read-only;
	/// consumers do not need to participate in Controlify's internal update lifecycle.
	Vector3fc getState();
}
