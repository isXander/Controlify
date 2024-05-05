package dev.isxander.controlify.splitscreen.protocol.packets.play;

import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PawnboundConfigureSplitscreenPacket(int monitorX, int monitorY, SplitscreenPosition position) implements PawnboundPlayPacket {
    public static final StreamCodec<FriendlyByteBuf, PawnboundConfigureSplitscreenPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PawnboundConfigureSplitscreenPacket::monitorX,

                    ByteBufCodecs.INT,
                    PawnboundConfigureSplitscreenPacket::monitorY,

                    SplitscreenPosition.STREAM_CODEC,
                    PawnboundConfigureSplitscreenPacket::position,

                    PawnboundConfigureSplitscreenPacket::new
            );
    public static final PacketType<PawnboundConfigureSplitscreenPacket> TYPE =
            PawnboundPlayPacket.createType("configure_splitscreen");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleConfigureSplitscreen(this);
    }

    @Override
    public PacketType<? extends Packet<PawnPlayPacketListener>> type() {
        return TYPE;
    }
}
