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
	private final ScreenContext<? extends CreateWorldScreen> context;

	private final Runnable onCreateButton;

	public CreateWorldScreenBehaviour(ScreenContext<? extends CreateWorldScreen> context, Runnable onCreateButton) {
		this.context = context;
		this.onCreateButton = onCreateButton;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			context.playClickSound();
			this.onCreateButton.run();
			return;
		}
		next.run();
	}
}
