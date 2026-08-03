package dev.isxander.controlify.neoforge.platform.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record NeoforgePacketWrapper<T>(Identifier channel, T payload) implements CustomPacketPayload {
	public static <T> Type<NeoforgePacketWrapper<T>> createType(Identifier channel) {
		return new Type<>(channel);
	}

	public static <T> StreamCodec<FriendlyByteBuf, NeoforgePacketWrapper<T>> wrapCodec(Identifier channel, StreamCodec<FriendlyByteBuf, T> streamCodec) {
		return StreamCodec.of(
			(buf, wrapper) -> streamCodec.encode(buf, wrapper.payload),
			buf -> new NeoforgePacketWrapper<>(channel, streamCodec.decode(buf))
		);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return createType(channel);
	}
}
