package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundCloseGamePacket() implements PawnboundPlayPacket {
    public static final PawnboundCloseGamePacket INSTANCE = new PawnboundCloseGamePacket();
    public static final StreamCodec<FriendlyByteBuf, PawnboundCloseGamePacket> CODEC =
            StreamCodec.unit(INSTANCE);
    public static final PacketType<PawnboundCloseGamePacket> TYPE =
            PawnboundPlayPacket.createType("close_game");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleCloseGame(this);
    }

    @Override
    public PacketType<PawnboundCloseGamePacket> type() {
        return TYPE;
    }
}
