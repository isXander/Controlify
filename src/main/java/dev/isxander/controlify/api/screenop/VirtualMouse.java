/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.gui.navigation.ScreenDirection;

/// The shared cursor for one screen. Mode selects which cursor behaviours run;
/// enabled state also reflects the current input mode and screen activation.
public interface VirtualMouse {
	VirtualMouseMode mode();

	void setMode(VirtualMouseMode mode);

	boolean isEnabled();

	int getCurrentX(float deltaTime);

	int getCurrentY(float deltaTime);

	void snapToClosestPoint();

	void snapInDirection(ScreenDirection direction);

	/// Explicit click fallback, including when automatic click emulation is disabled.
	void emulateClicks(Controller controller);

	void preventScrollingThisTick();
}
