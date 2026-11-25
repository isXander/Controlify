/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public final class ExampleKeyMappings {
	/// Read directly by {@link Boats#tick}.
	public static KeyMapping HORN;
	/// Other mods read `ZOOM.isDown()`.
	public static KeyMapping ZOOM;
	/// Opens the boat settings screen. Controlify creates an action for it automatically.
	public static KeyMapping SETTINGS;

	public static void register() {
		SETTINGS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.examplemod.settings", InputConstants.KEY_B, KeyMapping.Category.MISC));
		HORN = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.examplemod.horn", InputConstants.KEY_H, KeyMapping.Category.MISC));
		ZOOM = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.examplemod.zoom", InputConstants.KEY_C, KeyMapping.Category.MISC));
	}

	private ExampleKeyMappings() {
	}
}
