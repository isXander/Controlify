package dev.isxander.controlify.splitscreen.server.login.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ServerboundNonceAckPacket() implements CodedPacket<ServerboundNonceAckPacket> {
    public static final ServerboundNonceAckPacket UNIT = new ServerboundNonceAckPacket();

    public static final StreamCodec<FriendlyByteBuf, ServerboundNonceAckPacket> STREAM_CODEC =
            StreamCodec.unit(UNIT);

    @Override
    public StreamCodec<FriendlyByteBuf, ServerboundNonceAckPacket> codec() {
        return STREAM_CODEC;
    }
}
