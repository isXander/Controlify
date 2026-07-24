/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform.main.events;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface PlayerJoinedEvent {
	void onInit(ServerPlayer player);
}
