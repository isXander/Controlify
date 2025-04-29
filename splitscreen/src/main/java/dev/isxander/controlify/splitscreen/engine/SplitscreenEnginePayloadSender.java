package dev.isxander.controlify.splitscreen.engine;

import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.PawnboundEngineCustomPayloadPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface SplitscreenEnginePayloadSender {
    static SplitscreenEnginePayloadSender controllerbound(Connection connection) {
        return payload -> connection.send(new ControllerboundEngineCustomPayloadPacket(payload));
    }

    static SplitscreenEnginePayloadSender pawnbound(Connection connection) {
        return payload -> connection.send(new PawnboundEngineCustomPayloadPacket(payload));
    }

    void sendPayload(CustomPacketPayload payload);
}
