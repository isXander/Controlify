package dev.isxander.controlify.splitscreen.protocol.packets.play;

import dev.isxander.controlify.splitscreen.RemoteSplitscreenPawn;
import dev.isxander.controlify.splitscreen.SplitscreenController;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.protocol.packets.common.ControllerboundCommonPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerPacketListener;

public class ControllerPlayPacketListener implements ControllerboundCommonPacketListener, BidirectionalPlayPacketListener, ServerPacketListener {
    private final SplitscreenController controller;
    private SplitscreenPawn pawnInstance;
    private final Connection connection;

    public ControllerPlayPacketListener(SplitscreenController controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    public void handleHello(ControllerboundHelloPacket packet) {
        this.pawnInstance = new RemoteSplitscreenPawn(connection);
        controller.addPawn(pawnInstance);

        // initiate keep alive chain
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    public void handleKeepAlive(ControllerboundKeepAlivePacket packet) {
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        controller.removePawn(pawnInstance);
    }

    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
