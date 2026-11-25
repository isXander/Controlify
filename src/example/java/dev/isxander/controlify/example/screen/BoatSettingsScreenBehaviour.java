/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.screen;

import dev.isxander.controlify.api.action.BuiltinActions;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.example.settings.BoatSettings;
import dev.isxander.controlify.example.settings.BoatSettingsScreen;

// #region screen-behaviour
/// Resets the settings with the first GUI abstract action, and shows that on the reset button
/// while there is something to reset.
public class BoatSettingsScreenBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends BoatSettingsScreen> context;

	public BoatSettingsScreenBehaviour(ScreenContext<? extends BoatSettingsScreen> context) {
		this.context = context;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (BuiltinActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			this.context.screen().reset();
			this.context.playClickSound();
		}
		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		if (this.context.screen().resetButton != null) {
			this.context.buttonGuide().addGuideToButton(
					this.context.screen().resetButton,
					BuiltinActions.GUI_ABSTRACT_ACTION_1,
					button -> !BoatSettings.isDefault()
			);
		}
		next.run();
	}
}
// #endregion screen-behaviour
