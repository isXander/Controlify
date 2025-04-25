package dev.isxander.controlify.splitscreen.remote.ipc;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundKeepAlivePacket;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.*;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

/**
 * Handles incoming packets from the controller to a remote pawn (this client).
 * This class is responsible for processing the packets and
 * performing the appropriate actions.
 */
public class PawnPlayPacketListener implements PawnboundCommonPacketListener, ClientboundPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final LocalSplitscreenPawn pawn;
    private final Connection connection;
    private final Minecraft minecraft;

    public PawnPlayPacketListener(Connection connection, Minecraft minecraft) {
        this.pawn = new LocalSplitscreenPawn(minecraft);
        this.connection = connection;
        this.minecraft = minecraft;
    }

    public void handleJoinServer(PawnboundJoinServerPacket packet) {
        LOGGER.info("Pawn joining server server {}:{}", packet.host(), packet.port());
        minecraft.execute(() -> this.pawn.joinServer(packet.host(), packet.port()));
    }

    public void handleSplitscreenPosition(PawnboundSplitscreenPositionPacket packet) {
        LOGGER.info("Pawn setting splitscreen position to {}", packet.position());
        this.minecraft.execute(() ->
                this.pawn.setWindowSplitscreenMode(packet.position(), packet.parentWidth(), packet.parentHeight())
        );
    }

    public void handleParentWindow(PawnboundParentWindowPacket packet) {
        this.minecraft.execute(() ->
                this.pawn.setupWindowParent(
                        packet.parentWindowHandle(),
                        packet.x(), packet.y(),
                        packet.width(), packet.height()
                )
        );
    }

    public void handleWindowFocusState(PawnboundWindowFocusStatePacket packet) {
        this.pawn.setWindowFocusState(packet.focused());
    }

    public void handleCloseGame(PawnboundCloseGamePacket packet) {
        this.pawn.closeGame();
    }

    public void handleUseController(PawnboundUseControllerPacket packet) {
        LOGGER.info("Pawn using controller {}", packet.controllerUID());
        this.pawn.useController(packet.controllerUID());
    }

    public void handleKeepAlive(PawnboundKeepAlivePacket packet) {
        this.connection.send(ControllerboundKeepAlivePacket.INSTANCE);
    }

    @Override
    public void handleDisconnect(PawnboundDisconnectPacket packet) {
        this.connection.disconnect(Component.empty());
    }

    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        // If we disconnect for whatever reason, we can't leave a hanging pawn.
        this.minecraft.stop();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
