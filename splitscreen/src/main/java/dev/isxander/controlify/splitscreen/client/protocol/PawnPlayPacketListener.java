package dev.isxander.controlify.splitscreen.client.protocol;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.protocol.controllerbound.play.ControllerboundKeepAlivePacket;
import dev.isxander.controlify.splitscreen.protocol.pawnbound.play.*;
import dev.isxander.controlify.splitscreen.server.protocol.PawnboundCommonPacketListener;
import dev.isxander.controlify.splitscreen.protocol.pawnbound.common.PawnboundDisconnectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

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
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
