package dev.isxander.controlify.splitscreen.remote.ipc;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundKeepAlivePacket;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.*;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import dev.isxander.controlify.splitscreen.remote.RemotePawnMain;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.PacketUtils;
import org.slf4j.Logger;

/**
 * Handles incoming packets from the controller to a remote pawn (this client).
 * This class is responsible for processing the packets and
 * performing the appropriate actions.
 */
public class PawnPlayPacketListener implements PawnboundCommonPacketListener, ClientboundPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final RemotePawnMain remotePawnMain;
    private final LocalSplitscreenPawn pawn;
    private final Connection connection;
    private final Minecraft minecraft;

    public PawnPlayPacketListener(Connection connection, RemotePawnMain remotePawnMain, Minecraft minecraft) {
        this.remotePawnMain = remotePawnMain;
        this.pawn = remotePawnMain.getPawn();
        this.connection = connection;
        this.minecraft = minecraft;
    }

    public void handleJoinServer(PawnboundJoinServerPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        LOGGER.info("Pawn joining server server {}:{}", packet.host(), packet.port());
        this.pawn.joinServer(packet.host(), packet.port(), packet.nonce().orElse(null));
    }

    public void handleServerDisconnect(PawnboundServerDisconnectPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        LOGGER.info("Pawn disconnecting from server");
        this.pawn.disconnectFromServer();
    }

    public void handleEngineCustomPayload(PawnboundEngineCustomPayloadPacket packet) {
        this.remotePawnMain.getSplitscreenEngine().handleInboundPayload(packet.payload());
    }

    public void handleCloseGame(PawnboundCloseGamePacket packet) {
        this.pawn.closeGame();
    }

    public void handleUseController(PawnboundUseControllerPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        LOGGER.info("Pawn using controller {}", packet.controllerUID());
        this.pawn.useController(packet.controllerUID());
    }

    public void handleKeepAlive(PawnboundKeepAlivePacket packet) {
        this.connection.send(ControllerboundKeepAlivePacket.INSTANCE);
    }

    public void handleLoadConfig(PawnboundLoadConfigPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, minecraft);

        this.pawn.onConfigSave(packet.config());
    }

    @Override
    public void handleDisconnect(PawnboundDisconnectPacket packet) {
        System.out.println("Disconnecting from server: " + packet.reason().getString());
        this.connection.disconnect(packet.reason());
    }

    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        // If we disconnect for whatever reason, we can't leave a hanging pawn.
        System.out.println("Disconnecting from server2: " + details.reason().getString());
        this.minecraft.stop();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
