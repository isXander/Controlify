package dev.isxander.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.splitscreen.host.ipc.ControllerPlayPacketListener;
import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;

public interface ControllerboundPlayPacket extends Packet<ControllerPlayPacketListener> {
    static <T extends ControllerboundPlayPacket> PacketType<T> createType(String id) {
        return new PacketType<>(PacketFlow.SERVERBOUND, CSUtil.rl(id));
    }
}
