/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.neoforge.platform.network;

import dev.isxander.controlify.platform.network.S2CNetworkApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class S2CNetworkApiNeoforge implements S2CNetworkApi {
	@Override
	public <T> void sendPacket(ServerPlayer recipient, Identifier channel, T packet) {
		// TODO
	}

	@Override
	public <T> void listenForPacket(Identifier channel, PacketListener<T> listener) {
		// TODO
	}

	@Override
	public <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> handler) {
		// TODO
	}
}
