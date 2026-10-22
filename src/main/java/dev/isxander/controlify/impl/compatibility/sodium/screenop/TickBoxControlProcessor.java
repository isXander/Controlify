/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.screenop;

import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;

public class TickBoxControlProcessor implements ComponentProcessor {
	private final Runnable toggleMethod;

	public TickBoxControlProcessor(Runnable toggleMethod) {
		this.toggleMethod = toggleMethod;
	}

	@Override
	public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerImpl controller) {
		if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumePulse()) {
			toggleMethod.run();
			return true;
		}
		if (ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.on(controller).pulse().consumePulse() || ControlifyActions.GUI_SECONDARY_NAVI_LEFT.on(controller).pulse().consumePulse()) {
			toggleMethod.run();
			return true;
		}

		return false;
	}
}
