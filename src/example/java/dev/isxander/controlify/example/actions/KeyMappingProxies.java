/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.actions;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.action.ActionEvents;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.BuiltinActionLayers;
import dev.isxander.controlify.example.ExampleKeyMappings;
import net.minecraft.client.KeyMapping;

import static dev.isxander.controlify.example.ExampleMod.id;

/// How each of the example mod's key mappings relates to a controller action.
public final class KeyMappingProxies {
	public static ActionAccessor ZOOM;

	public static void registerEvents() {
		ActionEvents.REGISTER_ACTIONS.subscribeForever(registry -> {
			ZOOM = registry.register(ActionSpec.builder()
					.id(id("zoom"))
					.category(KeyMapping.Category.MISC.label())
					.layer(BuiltinActionLayers.IN_GAME)
					.build());
		});

		// #region proxy
		ActionEvents.REGISTER_PROXIES.subscribeForever(proxies -> {
			proxies.dontAutoProxyKeyMapping(ExampleKeyMappings.HORN);
			proxies.proxyKeyMapping(ZOOM, ExampleKeyMappings.ZOOM);
		});
		// #endregion proxy
	}

	private KeyMappingProxies() {
	}
}
