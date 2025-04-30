package dev.isxander.controlify.splitscreen.ipc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public interface ConnectionDisconnectPacketFactory {
    Packet<?> createDisconnectPacket(Throwable throwable, Component reason, boolean login);
}
