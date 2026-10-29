/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public final class ServerAddressBehaviour implements ScreenBehaviour {
	private final ServerAddressScreenAccess access;
	private final ScreenContext<? extends Screen> context;
	private final Screen screen;

	public ServerAddressBehaviour(ScreenContext<? extends Screen> context) {
		this.context = context;
		this.screen = context.screen();
		this.access = (ServerAddressScreenAccess) context.screen();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {

		Button doneButton = access.controlify$getDoneButton();
		Button backButton = access.controlify$getBackButton();

		if (doneButton != null) {
			context.buttonGuide().addGuideToButton(
					doneButton,
					() -> doneButton.isFocused()
							? ControlifyActions.GUI_PRESS
							: ControlifyActions.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		}

		if (backButton != null) {
			context.buttonGuide().addGuideToButton(
					backButton,
					() -> ControlifyActions.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		}
		next.run();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			Button doneButton = access.controlify$getDoneButton();
			if (doneButton != null && !doneButton.isFocused()) {
				ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses();
				context.playClickSound();
				screen.setFocused(doneButton);
			}
		}
		next.run();
	}
}
