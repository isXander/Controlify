/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.element;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.utils.MinecraftUtil;

import java.util.function.IntConsumer;

/// Discrete options share secondary navigation and repeated activation semantics.
public final class CycleBehaviour implements ElementBehaviour {
	private final IntConsumer cycle;

	public CycleBehaviour(IntConsumer cycle) {
		this.cycle = cycle;
	}

	@Override
	public void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (screen.isVirtualMouseEnabled()) {
			next.run();
			return;
		}
		boolean handled = consume(screen, controller, ControlifyActions.GUI_SECONDARY_NAVI_LEFT, -1);
		handled |= consume(screen, controller, ControlifyActions.GUI_SECONDARY_NAVI_RIGHT, 1);
		if (!handled) {
			next.run();
		}
	}

	@Override
	public void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (screen.isVirtualMouseEnabled() || !consume(screen, controller, ControlifyActions.GUI_PRESS, 1)) {
			next.run();
		}
	}

	private boolean consume(ScreenContext<?> screen, Controller controller, ActionAccessor action, int direction) {
		boolean handled = false;
		while (MinecraftUtil.getScreen() == screen.screen() && action.on(controller).pulse().consumePulse()) {
			cycle.accept(direction);
			screen.playNavigationHaptic(controller);
			handled = true;
		}
		return handled;
	}
}
