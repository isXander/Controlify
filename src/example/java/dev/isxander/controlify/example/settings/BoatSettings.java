/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.settings;

/// The example mod's settings. Kept in memory only.
public final class BoatSettings {
	public static float tiltSensitivity = 0.5f;
	public static String boatName = "Boat";

	public static boolean isDefault() {
		return tiltSensitivity == 0.5f && boatName.equals("Boat");
	}

	public static void reset() {
		tiltSensitivity = 0.5f;
		boatName = "Boat";
	}

	private BoatSettings() {
	}
}
