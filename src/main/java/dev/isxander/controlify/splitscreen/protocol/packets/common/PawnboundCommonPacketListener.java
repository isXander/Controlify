package dev.isxander.controlify.splitscreen.protocol.packets.common;

import net.minecraft.network.ClientboundPacketListener;

public interface PawnboundCommonPacketListener extends ClientboundPacketListener {
    void handleDisconnect(PawnboundDisconnectPacket packet);
}
