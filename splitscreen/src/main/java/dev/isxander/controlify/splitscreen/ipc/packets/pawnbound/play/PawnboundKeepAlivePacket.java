package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PawnboundKeepAlivePacket() implements PawnboundPlayPacket {
    public static final PawnboundKeepAlivePacket INSTANCE = new PawnboundKeepAlivePacket();
    public static final StreamCodec<FriendlyByteBuf, PawnboundKeepAlivePacket> CODEC = StreamCodec.unit(INSTANCE);
    public static final PacketType<PawnboundKeepAlivePacket> TYPE = PawnboundPlayPacket.createType("keep_alive");

    @Override
    public void handle(PawnPlayPacketListener listener) {
        listener.handleKeepAlive(this);
    }

    @Override
    public PacketType<? extends Packet<PawnPlayPacketListener>> type() {
        return TYPE;
    }
}
