package dev.isxander.controlify.splitscreen.ipc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public interface ConnectionDisconnectPacketFactory {
    Packet<?> createDisconnectPacket(Component reason, boolean login);
}
