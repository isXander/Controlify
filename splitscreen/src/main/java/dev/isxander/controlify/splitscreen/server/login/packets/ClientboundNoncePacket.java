package dev.isxander.controlify.splitscreen.server.login.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundNoncePacket(byte[] nonce) implements CodedPacket<ClientboundNoncePacket> {

    public static final int NONCE_SIZE_BITS = 128;
    public static final int NONCE_SIZE_BYTES = NONCE_SIZE_BITS / 8;

    public static final StreamCodec<FriendlyByteBuf, ClientboundNoncePacket> STREAM_CODEC =
            ByteBufCodecs.byteArray(NONCE_SIZE_BYTES)
                    .map(ClientboundNoncePacket::new, ClientboundNoncePacket::nonce)
                    .mapStream(FriendlyByteBuf::unwrap);

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundNoncePacket> codec() {
        return STREAM_CODEC;
    }
}
