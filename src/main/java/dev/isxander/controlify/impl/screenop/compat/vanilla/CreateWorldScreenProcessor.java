/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.bindings.ControlifyBindings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

public class CreateWorldScreenProcessor extends ScreenProcessor<CreateWorldScreen> {
	private final Runnable onCreateButton;

	public CreateWorldScreenProcessor(CreateWorldScreen screen, Runnable onCreateButton) {
		super(screen);
		this.onCreateButton = onCreateButton;
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
			playClackSound();
			this.onCreateButton.run();
		}

		super.handleButtons(controller);
	}
}
