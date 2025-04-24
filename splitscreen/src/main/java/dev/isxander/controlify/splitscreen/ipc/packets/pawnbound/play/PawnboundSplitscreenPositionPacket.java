package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundSplitscreenPositionPacket(
        int parentWidth, int parentHeight,
        SplitscreenPosition position
) implements PawnboundPlayPacket {
    public static final StreamCodec<ByteBuf, PawnboundSplitscreenPositionPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PawnboundSplitscreenPositionPacket::parentWidth,
                    ByteBufCodecs.INT,
                    PawnboundSplitscreenPositionPacket::parentHeight,
                    SplitscreenPosition.STREAM_CODEC,
                    PawnboundSplitscreenPositionPacket::position,

                    PawnboundSplitscreenPositionPacket::new
            );
    public static final PacketType<PawnboundSplitscreenPositionPacket> TYPE =
            PawnboundPlayPacket.createType("splitscreen_position");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleSplitscreenPosition(this);
    }

    @Override
    public PacketType<PawnboundSplitscreenPositionPacket> type() {
        return TYPE;
    }
}
