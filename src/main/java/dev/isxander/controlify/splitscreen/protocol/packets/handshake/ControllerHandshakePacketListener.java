package dev.isxander.controlify.splitscreen.protocol.packets.handshake;

import dev.isxander.controlify.splitscreen.SplitscreenController;
import dev.isxander.controlify.splitscreen.protocol.SplitscreenProtocol;
import dev.isxander.controlify.splitscreen.protocol.packets.common.ControllerboundCommonPacketListener;
import dev.isxander.controlify.splitscreen.protocol.packets.common.PawnboundDisconnectPacket;
import dev.isxander.controlify.splitscreen.protocol.packets.play.ControllerPlayPacketListener;
import dev.isxander.controlify.splitscreen.protocol.packets.play.PlayProtocols;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
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

        if (packet.protocolVersion() != SplitscreenProtocol.VERSION) {
            this.connection.send(new PawnboundDisconnectPacket());
            this.connection.disconnect(Component.empty());
            return;
        }

        this.connection.setupInboundProtocol(PlayProtocols.CONTROLLERBOUND, new ControllerPlayPacketListener(controller, connection));
    }

    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.HANDSHAKING;
    }

    @Override
    public void onDisconnect(Component reason) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
