/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.neoforge.platform.network;

import dev.isxander.controlify.platform.network.C2SNetworkApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;

public class C2SNetworkApiNeoforge implements C2SNetworkApi {
	private final Map<Identifier, PacketListener<?>> packetListeners = new HashMap<>();

	@Override
	public <T> void sendPacket(Identifier channel, T packet) {
		ClientPacketDistributor.sendToServer(this.createPayload(channel, packet));
	}

	@Override
	public <T> CustomPacketPayload createPayload(Identifier channel, T packet) {
		return new NeoforgePacketWrapper<>(channel, packet);
	}

	@Override
	public <T> void listenForPacket(Identifier channel, PacketListener<T> listener) {
		PacketListener<T> existingListener = this.getPacketListener(channel);
		PacketListener<T> chainedListener = (payload, sender) -> {
			existingListener.listen(payload, sender);
			listener.listen(payload, sender);
		};
		this.packetListeners.put(channel, chainedListener);
	}

	@Override
	public <T> void registerPacket(Identifier channel, StreamCodec<FriendlyByteBuf, T> payloadCodec) {
		var type = NeoforgePacketWrapper.<T>createType(channel);
		var packetCodec = NeoforgePacketWrapper.wrapCodec(channel, payloadCodec);

		this.getModEventBus().addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar("1");

			registrar.playToServer(
				type, packetCodec, (packet, ctx) -> {
					this.<T>getPacketListener(channel).listen(packet.payload(), (ServerPlayer) ctx.player());
				}
			);
		});
	}

	@SuppressWarnings("unchecked")
	private <T> PacketListener<T> getPacketListener(Identifier channel) {
		return (PacketListener<T>) this.packetListeners.getOrDefault(channel, (payload, sender) -> {});
	}

	private IEventBus getModEventBus() {
		return ModLoadingContext.get().getActiveContainer().getEventBus();
	}
}
