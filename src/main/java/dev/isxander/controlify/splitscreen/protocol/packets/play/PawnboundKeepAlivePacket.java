package dev.isxander.controlify.splitscreen.protocol.packets.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PawnboundKeepAlivePacket implements PawnboundPlayPacket {
    public static final PawnboundKeepAlivePacket INSTANCE = new PawnboundKeepAlivePacket();
    public static final StreamCodec<ByteBuf, PawnboundKeepAlivePacket> CODEC = StreamCodec.unit(INSTANCE);
    public static final PacketType<PawnboundKeepAlivePacket> TYPE = PawnboundPlayPacket.createType("keep_alive");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleKeepAlive(this);
    }

    @Override
    public PacketType<? extends Packet<PawnPlayPacketListener>> type() {
        return TYPE;
    }
}
