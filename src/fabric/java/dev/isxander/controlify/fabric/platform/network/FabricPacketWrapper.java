/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fabric.platform.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class FabricPacketWrapper<T> {
	public final CustomPacketPayload.Type<FabricPacketPayloadWrapper> type;

	public FabricPacketWrapper(
			Identifier channel,
			StreamCodec<FriendlyByteBuf, T> codec,
			PayloadTypeRegistry<? extends FriendlyByteBuf> registry
	) {
		this.type = new CustomPacketPayload.Type<>(channel);
		StreamCodec<FriendlyByteBuf, FabricPacketPayloadWrapper> streamCodec = StreamCodec.of(
				(buf, wrapper) -> codec.encode(buf, wrapper.payload),
				buf -> new FabricPacketPayloadWrapper(codec.decode(buf))
		);

		registry.register(type, streamCodec);
	}

	public class FabricPacketPayloadWrapper implements CustomPacketPayload {
		public final T payload;

		public FabricPacketPayloadWrapper(T payload) {
			this.payload = payload;
		}

		@Override
		public @NotNull Type<? extends CustomPacketPayload> type() {
			return type;
		}
	}
}
