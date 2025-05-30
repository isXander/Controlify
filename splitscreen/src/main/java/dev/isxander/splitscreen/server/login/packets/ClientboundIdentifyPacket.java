package dev.isxander.splitscreen.server.login.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Sent to every client (even vanilla ones) as soon as they initiate the connection with
 * {@link net.minecraft.network.protocol.login.ServerboundHelloPacket}.
 * Vanilla clients always respond with a null payload if they do not understand the packet,
 * our modded clients respond with {@link ServerboundIdentifyPacket}.
 * @param protocolVersion the splitscreen protocol version this server is going to use.
 */
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
