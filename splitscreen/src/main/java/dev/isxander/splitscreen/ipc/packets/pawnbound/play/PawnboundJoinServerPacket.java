package dev.isxander.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.Optional;

public record PawnboundJoinServerPacket(String host, int port, Optional<byte[]> nonce) implements PawnboundPlayPacket {
    public static final StreamCodec<FriendlyByteBuf, PawnboundJoinServerPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    PawnboundJoinServerPacket::host,

                    ByteBufCodecs.INT,
                    PawnboundJoinServerPacket::port,

                    ByteBufCodecs.optional(ByteBufCodecs.byteArray(16)),
                    PawnboundJoinServerPacket::nonce,

                    PawnboundJoinServerPacket::new
            );
    public static final PacketType<PawnboundJoinServerPacket> TYPE = PawnboundPlayPacket.createType("join_server");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleJoinServer(this);
    }

    @Override
    public PacketType<? extends Packet<PawnPlayPacketListener>> type() {
        return TYPE;
    }
}
