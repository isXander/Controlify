/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.screen;

import dev.isxander.controlify.api.screenop.ElementBehaviours;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.controlify.example.settings.BoatSettingsScreen;
import dev.isxander.controlify.example.settings.NameField;
import dev.isxander.controlify.example.settings.SensitivityBar;

/// Controller support for the boat settings screen, kept out of the screen's own classes.
public final class BoatSettingsBehaviours {
	public static void registerEvents() {
		// #region register-screen
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribeForever(registry ->
				registry.<BoatSettingsScreen>register(BoatSettingsScreen.class, BoatSettingsScreenBehaviour::new));
		// #endregion register-screen

		// #region register-elements
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribeForever(event -> {
			event.registry().register(SensitivityBar.class,
					ElementBehaviours.enabledWidget(ElementBehaviours.slider(SensitivityBar::nudge)));
			event.registry().register(NameField.class, NameFieldBehaviour::new);
		});
		// #endregion register-elements

		// #region register-guide
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribeForever(registry ->
				registry.<BoatSettingsScreen>register(BoatSettingsScreen.class, BoatSettingsGuideBehaviour::new));
		// #endregion register-guide
	}

	private BoatSettingsBehaviours() {
	}
}
