/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.font;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/// Common-side component encoding shared by the API factories and client mixins.
public final class ActionGlyphsImpl {
	public static final Identifier WRAPPER_FONT = Identifier.fromNamespaceAndPath("controlify", "inputs");
	public static final String PLACEHOLDER_KEY = "controlify.placeholder";
	public static final String PLACEHOLDER_CONTROLLER_ACTIVE_KEY = "controlify.placeholder.controller_active";

	private ActionGlyphsImpl() {
	}

	public static Component action(Identifier action) {
		return Component.keybind(action.toString()).withStyle(style -> style.withFont(new FontDescription.Resource(WRAPPER_FONT)));
	}

	public static Component actionWithFallback(Identifier action, Component fallback) {
		return Component.translatableWithFallback(PLACEHOLDER_KEY, "%2$s", action(action), fallback);
	}

	public static Component actionWithActiveFallback(Identifier action, Component fallback) {
		return Component.translatableWithFallback(PLACEHOLDER_CONTROLLER_ACTIVE_KEY, "%2$s", action(action), fallback);
	}
}
