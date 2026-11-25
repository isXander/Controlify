/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.contextual;

import dev.isxander.controlify.api.contextual.BuiltinContextualDomains;
import dev.isxander.controlify.api.contextual.ContextualEvents;
import dev.isxander.controlify.example.Boats;

import static dev.isxander.controlify.example.ExampleMod.id;

/// Exposes state only this mod knows to the in-game contextual domain, for feedback rules, button guides and
/// trigger effects in resource packs. Whether the player is in a boat is already known to the domain, so that
/// fact is defined in data instead.
public final class BoatContext {
	public static void registerEvents() {
		// #region contributor
		ContextualEvents.REGISTER_DOMAINS.subscribeForever(registry ->
				BuiltinContextualDomains.IN_GAME.registerContributor((inGame, sink) ->
						sink.contributeFact(id("boosting"), Boats.isPlayerInBoostingBoat(inGame.player()))));
		// #endregion contributor
	}

	private BoatContext() {
	}
}
