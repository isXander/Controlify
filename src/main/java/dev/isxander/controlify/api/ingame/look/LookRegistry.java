/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

import net.minecraft.resources.Identifier;

/// Where addons register look sources and scalers. See [LookEvents#REGISTER].
/// Each controller gets its own instance, created from the factory.
public interface LookRegistry {
	/// @throws IllegalStateException if registration has closed or the id is already a source
	void registerSource(Identifier id, LookSourceFactory factory);

	/// @throws IllegalStateException if registration has closed or the id is already a scaler
	void registerScaler(Identifier id, LookScalerFactory factory);
}
