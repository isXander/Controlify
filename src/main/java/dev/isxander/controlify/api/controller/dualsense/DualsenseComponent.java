/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.dualsense;

public interface DualsenseComponent {
	/// Set the state of the controller's mute LED to either lit or unlit
	void setMicLightMode(MicLightMode mode);
}
