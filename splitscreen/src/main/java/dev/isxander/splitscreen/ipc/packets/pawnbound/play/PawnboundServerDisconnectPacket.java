package dev.isxander.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundServerDisconnectPacket() implements PawnboundPlayPacket {
    public static final PawnboundServerDisconnectPacket UNIT = new PawnboundServerDisconnectPacket();
    public static final StreamCodec<FriendlyByteBuf, PawnboundServerDisconnectPacket> CODEC = StreamCodec.unit(UNIT);

    public static final PacketType<PawnboundServerDisconnectPacket> TYPE =
            PawnboundPlayPacket.createType("server_disconnected");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleServerDisconnect(this);
    }

    @Override
    public PacketType<PawnboundServerDisconnectPacket> type() {
        return TYPE;
    }
}
