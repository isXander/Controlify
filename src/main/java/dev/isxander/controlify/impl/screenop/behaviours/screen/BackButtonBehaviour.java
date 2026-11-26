/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;

public final class BackButtonBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;

	public BackButtonBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (context.screen().shouldCloseOnEsc() && ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
			context.playClickSound();
			context.screen().onClose();
		}
		next.run();
	}
}
