package dev.isxander.controlify.splitscreen.host.ipc;

import dev.isxander.controlify.splitscreen.host.SplitscreenController;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.handshake.ControllerboundHandshakePacket;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import dev.isxander.controlify.splitscreen.ipc.packets.PlayProtocols;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerPacketListener;

/**
 * Handles the handshake process of the controllerbound protocol.
 */
public class ControllerHandshakePacketListener implements ControllerboundCommonPacketListener, ServerPacketListener {
    private final SplitscreenController controller;
    private final Connection connection;
    private final Minecraft minecraft;

    public ControllerHandshakePacketListener(SplitscreenController controller, Connection connection, Minecraft minecraft) {
        this.controller = controller;
        this.connection = connection;
        this.minecraft = minecraft;
    }

    public void handleHandshake(ControllerboundHandshakePacket packet) {
        this.connection.setupOutboundProtocol(PlayProtocols.PAWNBOUND);

        if (packet.protocolVersion() != 1) {
            var reason = Component.literal("Unsupported protocol version: " + packet.protocolVersion());
            this.connection.send(new PawnboundDisconnectPacket(reason));
            this.connection.disconnect(reason);
            return;
        }

        this.connection.setupInboundProtocol(PlayProtocols.CONTROLLERBOUND, new ControllerPlayPacketListener(controller, connection, minecraft));
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
