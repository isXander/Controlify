/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.virtualmouse;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.VirtualMouseState;

public final class CursorScrollBehaviour implements ScreenBehaviour {
	private final VirtualMouseState mouse;

	public CursorScrollBehaviour(ScreenContextImpl<?> context) {
		this.mouse = context.virtualMouse();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		mouse.addScroll(ControlifyActions.VMOUSE_SCROLL_UP.on(controller).continuous().getContinuous()
						- ControlifyActions.VMOUSE_SCROLL_DOWN.on(controller).continuous().getContinuous());
		next.run();
	}

	@Override
	public void update(float delta, Runnable next) {
		mouse.updateScroll(delta);
		next.run();
	}
}
