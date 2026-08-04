/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.neoforge.platform.network;

import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.platform.network.C2SNetworkApi;
import dev.isxander.controlify.platform.network.S2CNetworkApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;

public class S2CNetworkApiNeoforge implements S2CNetworkApi {
	private final Map<Identifier, PacketListener<?>> packetListeners = new HashMap<>();

	@Override
	public <T> void sendPacket(ServerPlayer recipient, Identifier channel, T packet) {
		PacketDistributor.sendToPlayer(recipient, new NeoforgePacketWrapper<>(channel, packet));
	}

	@Override
	public <T> void listenForPacket(Identifier channel, PacketListener<T> listener) {
		PacketListener<T> existingListener = this.getPacketListener(channel);
		PacketListener<T> chainedListener = (payload) -> {
			existingListener.listen(payload);
			listener.listen(payload);
		};
		this.packetListeners.put(channel, chainedListener);
	}

	@Override
	public <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> payloadCodec) {
		var type = NeoforgePacketWrapper.<T>createType(channel);
		var packetCodec = NeoforgePacketWrapper.wrapCodec(channel, payloadCodec);

		this.getModEventBus().addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar("1").optional();

			registrar.playToClient(type, packetCodec);
		});

		if (PlatformMainUtil.getEnv() == Environment.CLIENT) {
			this.getModEventBus().addListener(RegisterClientPayloadHandlersEvent.class, event -> {
				event.register(type, (packet, _) -> {
					this.<T>getPacketListener(channel).listen(packet.payload());
				});
			});
		}
	}

	@SuppressWarnings("unchecked")
	private <T> PacketListener<T> getPacketListener(Identifier channel) {
		return (PacketListener<T>) this.packetListeners.getOrDefault(channel, (payload) -> {});
	}

	private IEventBus getModEventBus() {
		return ModLoadingContext.get().getActiveContainer().getEventBus();
	}
}
