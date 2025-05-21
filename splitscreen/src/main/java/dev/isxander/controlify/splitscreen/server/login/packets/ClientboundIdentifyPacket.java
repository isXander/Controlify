package dev.isxander.controlify.splitscreen.server.login.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundIdentifyPacket(int protocolVersion) implements CodedPacket<ClientboundIdentifyPacket> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundIdentifyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundIdentifyPacket::protocolVersion,
            ClientboundIdentifyPacket::new
    );

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundIdentifyPacket> codec() {
        return STREAM_CODEC;
    }
}
