package dev.isxander.controlify.splitscreen.client.protocol.play;

import dev.isxander.controlify.splitscreen.server.protocol.play.ControllerPlayPacketListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;

public interface ControllerboundPlayPacket extends Packet<ControllerPlayPacketListener> {
    static <T extends ControllerboundPlayPacket> PacketType<T> createType(String id) {
        return new PacketType<>(PacketFlow.SERVERBOUND, CUtil.rl(id));
    }
}
