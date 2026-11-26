/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fabric.compat;

import dev.isxander.controlify.compat.CompatMixinPlatform;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricCompatMixinPlatform implements CompatMixinPlatform {
	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
}
