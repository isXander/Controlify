package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.common;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnboundCommonPacketListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public record PawnboundDisconnectPacket() implements Packet<PawnboundCommonPacketListener> {
    public static final StreamCodec<ByteBuf, PawnboundDisconnectPacket> CODEC = StreamCodec.unit(new PawnboundDisconnectPacket());
    // using minecraft:disconnect instead of controlify:disconnect as Connection sends a vanilla disconnect packet, this handles it
    public static final PacketType<PawnboundDisconnectPacket> TYPE = new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace("disconnect"));

    @Override
    public void handle(PawnboundCommonPacketListener handler) {
        handler.handleDisconnect(this);
    }

    @Override
    public PacketType<? extends Packet<PawnboundCommonPacketListener>> type() {
        return TYPE;
    }
}
