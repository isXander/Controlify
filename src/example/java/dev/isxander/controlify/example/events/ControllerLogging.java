/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.events;

import dev.isxander.controlify.api.controllermanager.ControllerEvents;
import dev.isxander.controlify.example.ExampleMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Logs controllers plugged in while the game is running.
public final class ControllerLogging {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMod.MOD_ID);

	public static void registerEvents() {
		// #region runtime-event
		ControllerEvents.CONTROLLER_CONNECTED
				.filter(ControllerEvents.ControllerConnected::hotplugged)
				.subscribeForever(event -> LOGGER.info("Connected {}", event.controller().name()));
		// #endregion runtime-event
	}

	private ControllerLogging() {
	}
}
