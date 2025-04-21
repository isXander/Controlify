package dev.isxander.controlify.splitscreen.client.protocol.handshake;

import dev.isxander.controlify.splitscreen.server.SplitscreenController;
import dev.isxander.controlify.splitscreen.client.protocol.common.ControllerboundCommonPacketListener;
import dev.isxander.controlify.splitscreen.server.protocol.common.PawnboundDisconnectPacket;
import dev.isxander.controlify.splitscreen.server.protocol.play.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.protocol.PlayProtocols;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerPacketListener;

public class ControllerHandshakePacketListener implements ControllerboundCommonPacketListener, ServerPacketListener {
    private final SplitscreenController controller;
    private final Connection connection;

    public ControllerHandshakePacketListener(SplitscreenController controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    public void handleHandshake(ControllerboundHandshakePacket packet) {
        this.connection.setupOutboundProtocol(PlayProtocols.PAWNBOUND);

        if (packet.protocolVersion() != 1) {
            this.connection.send(new PawnboundDisconnectPacket());
            this.connection.disconnect(Component.literal("Unsupported protocol version: " + packet.protocolVersion()));
            return;
        }

        this.connection.setupInboundProtocol(PlayProtocols.CONTROLLERBOUND, new ControllerPlayPacketListener(controller, connection));
    }

    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.HANDSHAKING;
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
