/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.gyro;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3fc;

import java.util.Optional;

/// Gyroscope input. Presence in Controller.gyro() indicates gyro support.
@ApiStatus.NonExtendable
public interface GyroComponent {
	/// The latest calibrated, filtered and integrated gyro interval in radians.
	/// Returns zero when the latest interval is older than 50ms. Read-only;
	/// consumers do not need to participate in Controlify's internal update lifecycle.
	Vector3fc getState();

	/// Which way is up, as a unit vector in the controller's frame: X right, Y up and Z towards the player
	/// when the controller is held upright in front of you. Estimated from the accelerometer and gyro, so it
	/// gives the controller's tilt directly, without integrating motion.
	/// Empty when the controller has no accelerometer, or no motion arrived recently.
	Optional<Vector3fc> gravity();
}
