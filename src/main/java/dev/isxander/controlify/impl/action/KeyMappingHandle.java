/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

/// Applies emulated output without changing Minecraft's keyboard toggle policy.
public interface KeyMappingHandle {
	void controlify$click();

	void controlify$setDown(boolean down);
}
