package dev.isxander.controlify.splitscreen.server.login.packets;

import dev.isxander.controlify.splitscreen.server.login.ClientIdentification;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ServerboundIdentifyPacket(ClientIdentification identification) implements CodedPacket<ServerboundIdentifyPacket> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundIdentifyPacket> STREAM_CODEC =
            ClientIdentification.STREAM_CODEC.map(ServerboundIdentifyPacket::new, ServerboundIdentifyPacket::identification);

    @Override
    public StreamCodec<FriendlyByteBuf, ServerboundIdentifyPacket> codec() {
        return STREAM_CODEC;
    }
}
