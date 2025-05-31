package dev.isxander.splitscreen.client.host.ipc;

import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.client.host.RemoteSplitscreenPawn;
import dev.isxander.splitscreen.client.host.SplitscreenController;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.*;
import dev.isxander.splitscreen.client.ipc.packets.pawnbound.play.PawnboundKeepAlivePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerPacketListener;
import org.slf4j.Logger;

/**
 * Handles incoming packets from a remote pawn to the controller during play phase.
 * This class is responsible for processing the packets and
 * performing the appropriate actions.
 */
public class ControllerPlayPacketListener implements ControllerboundCommonPacketListener, ServerPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SplitscreenController controller;
    private RemoteSplitscreenPawn pawnInstance;
    private final Connection connection;
    private final Minecraft minecraft;

    public ControllerPlayPacketListener(SplitscreenController controller, Connection connection, Minecraft minecraft) {
        this.controller = controller;
        this.connection = connection;
        this.minecraft = minecraft;
    }

    public void handleHello(ControllerboundHelloPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        this.pawnInstance = new RemoteSplitscreenPawn(this.connection, this.controller.getNextPawnIndex(), packet.controller());
        this.controller.addPawn(this.pawnInstance);

        // initiate keep alive chain
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    public void handleGiveChildFocusIfForeground(ControllerboundGiveMeFocusIfForegroundPacket packet) {
        this.minecraft.execute(() -> {
            //this.controller.getControllerBridge().giveFocusToChildIfForeground(packet.childWindow(), this.pawnInstance);
        });
    }

    public void handleReadySignal(ControllerboundSignalReadyPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        this.controller.getControllerBridge().signalRemoteClientReady(packet.finished(), packet.progress(), this.pawnInstance, this.pawnInstance.getAssociatedController());
    }

    public void handleKeepAlive(ControllerboundKeepAlivePacket packet) {
        this.connection.send(PawnboundKeepAlivePacket.INSTANCE);
    }

    public void handleEngineCustomPayload(ControllerboundEngineCustomPayloadPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        this.controller.getSplitscreenEngine().handleInboundPayload(this.pawnInstance.getAssociatedController(), this.connection, packet.payload());
    }

    public void handleServerDisconnected(ControllerboundServerDisconnectedPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        this.controller.getControllerBridge().serverDisconnectedRemote(packet.disconnectReason(), this.pawnInstance);
    }

    public void handleRequestMusic(ControllerboundRequestPlayMusicPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        this.controller.getControllerBridge().requestPlayMusicRemote(packet.music().orElse(null), packet.volume(), this.pawnInstance);
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        this.minecraft.execute(() -> {
            controller.removePawn(pawnInstance);
        });
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
