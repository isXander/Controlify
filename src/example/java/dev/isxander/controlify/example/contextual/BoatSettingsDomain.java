/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.contextual;

import dev.isxander.controlify.api.contextual.ContextualDomain;
import dev.isxander.controlify.api.contextual.ContextualEvents;
import dev.isxander.controlify.example.settings.NameField;
import dev.isxander.controlify.example.settings.SensitivityBar;

import static dev.isxander.controlify.example.ExampleMod.id;

/// A domain for the boat settings screen, so packs can write button guides for it.
public final class BoatSettingsDomain {
	// #region register
	public static ContextualDomain<BoatSettingsContext> DOMAIN;

	public static void registerEvents() {
		ContextualEvents.REGISTER_DOMAINS.subscribeForever(registry ->
				DOMAIN = registry.register(id("boat_settings"), (settings, sink) -> {
					sink.contributeFact(id("name_focused"), settings.focused() instanceof NameField);
					sink.contributeFact(id("sensitivity_focused"), settings.focused() instanceof SensitivityBar);
				}));
	}
	// #endregion register

	private BoatSettingsDomain() {
	}
}
