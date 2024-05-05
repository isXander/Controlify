package dev.isxander.controlify.splitscreen.protocol.packets.play;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public interface PawnboundPlayPacket extends Packet<PawnPlayPacketListener> {
    static <T extends PawnboundPlayPacket> PacketType<T> createType(String id) {
        return new PacketType<>(PacketFlow.CLIENTBOUND, new ResourceLocation("controlify", id));
    }
}
