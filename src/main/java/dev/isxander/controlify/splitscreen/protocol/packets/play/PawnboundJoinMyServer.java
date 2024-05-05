package dev.isxander.controlify.splitscreen.protocol.packets.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PawnboundJoinMyServer(int port) implements PawnboundPlayPacket {
    public static final StreamCodec<ByteBuf, PawnboundJoinMyServer> CODEC = ByteBufCodecs.INT
            .map(PawnboundJoinMyServer::new, PawnboundJoinMyServer::port);
    public static final PacketType<PawnboundJoinMyServer> TYPE = PawnboundPlayPacket.createType("join_my_server");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleJoinMyServer(this);
    }

    @Override
    public PacketType<? extends Packet<PawnPlayPacketListener>> type() {
        return TYPE;
    }
}
