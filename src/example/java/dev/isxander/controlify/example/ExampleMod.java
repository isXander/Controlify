/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.resources.Identifier;

/// The mod itself, which works without Controlify installed.
/// Nothing reachable from here references Controlify's API.
public class ExampleMod implements ClientModInitializer {
	public static final String MOD_ID = "examplemod";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitializeClient() {
		ExampleKeyMappings.register();
		ClientTickEvents.END_CLIENT_TICK.register(Boats::tick);
	}
}
