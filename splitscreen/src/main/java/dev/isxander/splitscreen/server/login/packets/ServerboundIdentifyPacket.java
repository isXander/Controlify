package dev.isxander.splitscreen.server.login.packets;

import dev.isxander.splitscreen.server.login.ClientIdentification;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Sent in response to {@link ClientboundIdentifyPacket} which contains information on if this client
 * is part of a splitscreen system and if so if it is acting as controller or a pawn.
 */
public record ServerboundIdentifyPacket(ClientIdentification identification) implements CodedPacket<ServerboundIdentifyPacket> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundIdentifyPacket> STREAM_CODEC =
            ClientIdentification.STREAM_CODEC.map(ServerboundIdentifyPacket::new, ServerboundIdentifyPacket::identification);

    @Override
    public StreamCodec<FriendlyByteBuf, ServerboundIdentifyPacket> codec() {
        return STREAM_CODEC;
    }
}
