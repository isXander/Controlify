package dev.isxander.splitscreen.remote.ipc;

import dev.isxander.splitscreen.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import net.minecraft.network.ClientboundPacketListener;

public interface PawnboundCommonPacketListener extends ClientboundPacketListener {
    void handleDisconnect(PawnboundDisconnectPacket packet);
}
