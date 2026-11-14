/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api;

/// Registers addon event listeners before Controlify starts client setup.
/// Use Fabric's `controlify` entrypoint or Java's ServiceLoader on either loader.
/// Called once per provider class, client-side, after platform services are installed.
/// Minecraft and Controlify's managers are not yet available; obtain managers from events.
@FunctionalInterface
public interface ControlifyEntrypoint {
	/// Register any events here
	///
	/// @see ControlifyEvents
	/// @see dev.isxander.controlify.api.action.ActionEvents
	/// @see dev.isxander.controlify.api.contextual.ContextualEvents
	/// @see dev.isxander.controlify.api.controllermanager.ControllerEvents
	/// @see dev.isxander.controlify.api.ingame.InGameEvents
	/// @see dev.isxander.controlify.api.screenop.ScreenopEvents
	void onControlifyInitialize();
}
