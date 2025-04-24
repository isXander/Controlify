package dev.isxander.controlify.splitscreen.server.protocol;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.protocol.controllerbound.play.ControllerboundGiveMeFocusIfForegroundPacket;
import dev.isxander.controlify.splitscreen.protocol.controllerbound.play.ControllerboundHelloPacket;
import dev.isxander.controlify.splitscreen.protocol.controllerbound.play.ControllerboundKeepAlivePacket;
import dev.isxander.controlify.splitscreen.server.LocalControllerBridge;
import dev.isxander.controlify.splitscreen.server.RemoteSplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.SplitscreenController;
import dev.isxander.controlify.splitscreen.client.protocol.ControllerboundCommonPacketListener;
import dev.isxander.controlify.splitscreen.protocol.pawnbound.play.PawnboundKeepAlivePacket;
import net.minecraft.client.Minecraft;
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
    private final Minecraft minecraft;

    public ControllerPlayPacketListener(SplitscreenController controller, Connection connection, Minecraft minecraft) {
        this.controller = controller;
        this.connection = connection;
        this.minecraft = minecraft;
    }

    public void handleHello(ControllerboundHelloPacket packet) {
        this.pawnInstance = new RemoteSplitscreenPawn(this.connection);
        this.controller.addPawn(this.pawnInstance);

        // initiate keep alive chain
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    public void handleGiveChildFocusIfForeground(ControllerboundGiveMeFocusIfForegroundPacket packet) {
        this.minecraft.execute(() -> {
            this.controller.getControllerBridge().giveFocusToChildIfForeground(packet.childWindow(), this.pawnInstance);
        });
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
