package dev.isxander.splitscreen.server.login.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Sent to the server after the client has received {@link ClientboundNoncePacket}.
 */
public record ServerboundNonceAckPacket() implements CodedPacket<ServerboundNonceAckPacket> {
    public static final ServerboundNonceAckPacket UNIT = new ServerboundNonceAckPacket();

    public static final StreamCodec<FriendlyByteBuf, ServerboundNonceAckPacket> STREAM_CODEC =
            StreamCodec.unit(UNIT);

    @Override
    public StreamCodec<FriendlyByteBuf, ServerboundNonceAckPacket> codec() {
        return STREAM_CODEC;
    }
}
