package dev.isxander.controlify.splitscreen.server.login.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundIdentifyPacket() implements CodedPacket<ClientboundIdentifyPacket> {
    public static final ClientboundIdentifyPacket UNIT = new ClientboundIdentifyPacket();
    public static final StreamCodec<FriendlyByteBuf, ClientboundIdentifyPacket> STREAM_CODEC = StreamCodec.unit(UNIT);

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundIdentifyPacket> codec() {
        return STREAM_CODEC;
    }
}
