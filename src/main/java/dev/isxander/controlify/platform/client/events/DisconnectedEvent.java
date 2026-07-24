/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface DisconnectedEvent {
	void onDisconnected(Minecraft minecraft);
}
