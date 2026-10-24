/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.virtualmouse;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.VirtualMouseState;

public final class CursorClickBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;
	private final VirtualMouseState mouse;

	public CursorClickBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
		this.mouse = context.virtualMouse();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (context.isCurrentScreen()) {
			mouse.emulateClicks(controller);
		}
		next.run();
	}
}
