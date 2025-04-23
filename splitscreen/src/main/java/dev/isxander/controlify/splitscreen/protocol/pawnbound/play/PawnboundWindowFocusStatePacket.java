package dev.isxander.controlify.splitscreen.protocol.pawnbound.play;

import dev.isxander.controlify.splitscreen.client.protocol.PawnPlayPacketListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundWindowFocusStatePacket(boolean focused) implements PawnboundPlayPacket {
    public static final StreamCodec<ByteBuf, PawnboundWindowFocusStatePacket> CODEC =
            ByteBufCodecs.BOOL.map(PawnboundWindowFocusStatePacket::new, PawnboundWindowFocusStatePacket::focused);
    public static final PacketType<PawnboundWindowFocusStatePacket> TYPE =
            PawnboundPlayPacket.createType("window_focus_state");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleWindowFocusState(this);
    }

    @Override
    public PacketType<PawnboundWindowFocusStatePacket> type() {
        return TYPE;
    }
}
