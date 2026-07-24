/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform.network;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public interface S2CNetworkApi extends SidedNetworkApi {
	<T> void sendPacket(ServerPlayer recipient, Identifier channel, T packet);

	<T> void listenForPacket(Identifier channel, PacketListener<T> listener);

	@FunctionalInterface
	interface PacketListener<T> {
		void listen(T packet);
	}
}
