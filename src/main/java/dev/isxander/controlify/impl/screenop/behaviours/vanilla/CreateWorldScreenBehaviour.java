/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

public final class CreateWorldScreenBehaviour implements ScreenBehaviour {
	private final Access access;
	private final ScreenContext<? extends CreateWorldScreen> context;

	public CreateWorldScreenBehaviour(ScreenContext<? extends CreateWorldScreen> context) {
		this.context = context;
		this.access = (Access) context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			context.playClickSound();
			access.controlify$createWorld();
			return;
		}
		next.run();
	}

	public interface Access {
		void controlify$createWorld();
	}
}
