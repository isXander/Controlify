/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.dualsense;

public enum MicLightMode {
	OFF,
	ON,
	BREATHING;

	public static MicLightMode fromBoolean(boolean on) {
		return on ? ON : OFF;
	}
}
