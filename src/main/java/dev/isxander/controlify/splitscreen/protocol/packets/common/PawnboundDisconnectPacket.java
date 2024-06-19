package dev.isxander.controlify.splitscreen.protocol.packets.common;

import dev.isxander.controlify.utils.CUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public record PawnboundDisconnectPacket() implements Packet<PawnboundCommonPacketListener> {
    public static final StreamCodec<ByteBuf, PawnboundDisconnectPacket> CODEC = StreamCodec.unit(new PawnboundDisconnectPacket());
    public static final PacketType<PawnboundDisconnectPacket> TYPE = new PacketType<>(PacketFlow.CLIENTBOUND, CUtil.rl("disconnect"));

    @Override
    public void handle(PawnboundCommonPacketListener handler) {
        handler.handleDisconnect(this);
    }

    @Override
    public PacketType<? extends Packet<PawnboundCommonPacketListener>> type() {
        return TYPE;
    }
}
