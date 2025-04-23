package dev.isxander.controlify.splitscreen.server.protocol;

import dev.isxander.controlify.splitscreen.protocol.pawnbound.common.PawnboundDisconnectPacket;
import net.minecraft.network.ClientboundPacketListener;

public interface PawnboundCommonPacketListener extends ClientboundPacketListener {
    void handleDisconnect(PawnboundDisconnectPacket packet);
}
