/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.BuiltinActionLayers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinContextOrderTest {
	@Test
	void builtinStackMatchesDocumentedOrder() {
		var registry = ActionLayerRegistryImpl.INSTANCE;
		registry.registerBuiltinContexts();
		registry.lock();

		assertEquals(List.of(
				BuiltinActionLayers.UNKNOWN,
				BuiltinActionLayers.RADIAL_MENU,
				BuiltinActionLayers.V_MOUSE_COMPAT,
				BuiltinActionLayers.V_MOUSE_CURSOR,
				BuiltinActionLayers.CONTAINER,
				BuiltinActionLayers.REGULAR_SCREEN,
				BuiltinActionLayers.ANY_SCREEN,
				BuiltinActionLayers.IN_GAME
		), registry.getOrderedContexts().stream().map(ActionLayer::id).toList());
	}
}
