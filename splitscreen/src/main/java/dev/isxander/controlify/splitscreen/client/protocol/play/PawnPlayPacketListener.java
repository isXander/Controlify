package dev.isxander.controlify.splitscreen.client.protocol.play;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.client.ClientSplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.protocol.common.PawnboundCommonPacketListener;
import dev.isxander.controlify.splitscreen.server.protocol.common.PawnboundDisconnectPacket;
import dev.isxander.controlify.splitscreen.server.protocol.play.PawnboundJoinServerPacket;
import dev.isxander.controlify.splitscreen.server.protocol.play.PawnboundKeepAlivePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class PawnPlayPacketListener implements PawnboundCommonPacketListener, ClientboundPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ClientSplitscreenPawn pawn;
    private final Connection connection;

    public PawnPlayPacketListener(Connection connection, Minecraft minecraft) {
        this.pawn = new ClientSplitscreenPawn(minecraft);
        this.connection = connection;
    }

    public void handleJoinServer(PawnboundJoinServerPacket packet) {
        LOGGER.info("Pawn joined server {}:{}", packet.host(), packet.port());
        this.pawn.joinServer(packet.host(), packet.port());
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
