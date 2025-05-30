package dev.isxander.splitscreen.client.remote.ipc;

import dev.isxander.splitscreen.client.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import net.minecraft.network.ClientboundPacketListener;

public interface PawnboundCommonPacketListener extends ClientboundPacketListener {
    void handleDisconnect(PawnboundDisconnectPacket packet);
}
