package dev.isxander.controlify.splitscreen.server.protocol.common;

import net.minecraft.network.ClientboundPacketListener;

public interface PawnboundCommonPacketListener extends ClientboundPacketListener {
    void handleDisconnect(PawnboundDisconnectPacket packet);
}
