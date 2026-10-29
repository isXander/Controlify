/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.VirtualMouseMode;

/// Physical input is handled by a dedicated subscription rather than GUI actions.
public final class InputCaptureBehaviour implements ScreenBehaviour {
	public InputCaptureBehaviour(ScreenContext<?> context) {
		context.virtualMouse().setMode(VirtualMouseMode.DISABLED);
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		// Captured input must not also navigate the screen.
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		// Captured input must not also activate controls.
	}
}
