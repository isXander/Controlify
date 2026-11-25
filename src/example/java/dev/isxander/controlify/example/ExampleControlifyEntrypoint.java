/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example;

import dev.isxander.controlify.api.ControlifyEntrypoint;
import dev.isxander.controlify.example.actions.BoatActions;
import dev.isxander.controlify.example.actions.KeyMappingProxies;
import dev.isxander.controlify.example.contextual.BoatContext;
import dev.isxander.controlify.example.contextual.BoatSettingsDomain;
import dev.isxander.controlify.example.events.ControllerLogging;
import dev.isxander.controlify.example.feedback.BoatFeedback;
import dev.isxander.controlify.example.look.TiltSteeringLook;
import dev.isxander.controlify.example.look.ZoomLook;
import dev.isxander.controlify.example.screen.BoatSettingsBehaviours;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

// #region entrypoint
public class ExampleControlifyEntrypoint implements ControlifyEntrypoint {
	@Override
	public void onControlifyInitialize() {
		BoatActions.registerEvents();
		KeyMappingProxies.registerEvents();
		ControllerLogging.registerEvents();
		BoatSettingsBehaviours.registerEvents();
		BoatFeedback.registerEvents();
		BoatContext.registerEvents();
		BoatSettingsDomain.registerEvents();
		ZoomLook.registerEvents();
		TiltSteeringLook.registerEvents();

		ClientTickEvents.END_CLIENT_TICK.register(BoatActions::tick);
		ClientTickEvents.END_CLIENT_TICK.register(BoatActions::showHintOnBoarding);
	}
}
// #endregion entrypoint
