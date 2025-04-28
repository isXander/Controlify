package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import dev.isxander.controlify.splitscreen.util.CSUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;

public interface PawnboundPlayPacket extends Packet<PawnPlayPacketListener> {
    static <T extends PawnboundPlayPacket> PacketType<T> createType(String id) {
        return new PacketType<>(PacketFlow.CLIENTBOUND, CSUtil.rl(id));
    }
}
