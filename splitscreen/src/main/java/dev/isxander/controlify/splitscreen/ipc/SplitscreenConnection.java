package dev.isxander.controlify.splitscreen.ipc;

import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class SplitscreenConnection extends Connection implements ConnectionDisconnectPacketFactory {
    public SplitscreenConnection(PacketFlow receiving) {
        super(receiving);
    }

    @Override
    public Packet<?> createDisconnectPacket(Component reason, boolean login) {
        return new PawnboundDisconnectPacket(reason);
    }

}
