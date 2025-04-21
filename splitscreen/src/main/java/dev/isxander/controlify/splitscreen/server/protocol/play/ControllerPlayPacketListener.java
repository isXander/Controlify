package dev.isxander.controlify.splitscreen.server.protocol.play;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.client.protocol.play.ControllerboundHelloPacket;
import dev.isxander.controlify.splitscreen.client.protocol.play.ControllerboundKeepAlivePacket;
import dev.isxander.controlify.splitscreen.server.ServerSplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.SplitscreenController;
import dev.isxander.controlify.splitscreen.client.protocol.common.ControllerboundCommonPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.game.ServerPacketListener;
import org.slf4j.Logger;

public class ControllerPlayPacketListener implements ControllerboundCommonPacketListener, ServerPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SplitscreenController controller;
    private SplitscreenPawn pawnInstance;
    private final Connection connection;

    public ControllerPlayPacketListener(SplitscreenController controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    public void handleHello(ControllerboundHelloPacket packet) {
        this.pawnInstance = new ServerSplitscreenPawn(this.connection);
        this.controller.addPawn(this.pawnInstance);

        // initiate keep alive chain
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    public void handleKeepAlive(ControllerboundKeepAlivePacket packet) {
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
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
