/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;

public final class FocusedElementBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;

	public FocusedElementBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		context.dispatchElements(controller, true, next);
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		context.dispatchElements(controller, false, next);
	}
}
