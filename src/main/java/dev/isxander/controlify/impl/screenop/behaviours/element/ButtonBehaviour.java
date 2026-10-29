/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.element;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.action.ControlifyActions;

import java.util.function.Consumer;

public final class ButtonBehaviour implements ElementBehaviour {
	private final Runnable press;
	private final Consumer<ScreenContext<?>> sound;

	public ButtonBehaviour(Runnable press) {
		this(press, ScreenContext::playClickSound);
	}

	public ButtonBehaviour(Runnable press, Runnable sound) {
		this(press, screen -> sound.run());
	}

	private ButtonBehaviour(Runnable press, Consumer<ScreenContext<?>> sound) {
		this.press = press;
		this.sound = sound;
	}

	@Override
	public void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (!screen.isVirtualMouseEnabled() && ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses()) {
			sound.accept(screen);
			screen.playNavigationHaptic(controller);
			press.run();
			return;
		}
		next.run();
	}
}
