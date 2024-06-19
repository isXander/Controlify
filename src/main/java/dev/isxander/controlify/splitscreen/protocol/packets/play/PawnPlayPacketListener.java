package dev.isxander.controlify.splitscreen.protocol.packets.play;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.mixins.feature.splitscreen.ScreenManagerAccessor;
import dev.isxander.controlify.mixins.feature.splitscreen.WindowAccessor;
import dev.isxander.controlify.splitscreen.ClientSplitscreenPawn;
import dev.isxander.controlify.splitscreen.protocol.packets.common.PawnboundCommonPacketListener;
import dev.isxander.controlify.splitscreen.protocol.packets.common.PawnboundDisconnectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class PawnPlayPacketListener implements PawnboundCommonPacketListener, BidirectionalPlayPacketListener, ClientboundPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ClientSplitscreenPawn pawn;
    private final Connection connection;

    public PawnPlayPacketListener(Connection connection) {
        this.pawn = new ClientSplitscreenPawn();
        this.connection = connection;
    }

    public void handleJoinMyServer(PawnboundJoinMyServer packet) {
        pawn.joinMyServer(packet.port());
    }

    @SuppressWarnings("UnreachableCode")
    public void handleConfigureSplitscreen(PawnboundConfigureSplitscreenPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ScreenManager screenManager = ((WindowAccessor) (Object) minecraft.getWindow()).getScreenManager();
        Collection<Monitor> monitors = ((ScreenManagerAccessor) screenManager).getMonitors().values();

        // the monitor handles differ between clients so there is no reliable way to pass
        // references over the network. this is the next best thing. find the monitor with the same
        // absolute screen coordinates and use that. I don't *think* it's possible for two monitors to have
        // the same XY, but it's the best I can do.
        Optional<Monitor> monitor = monitors.stream()
                .filter(m -> m.getX() == packet.monitorX() && m.getY() == packet.monitorY())
                .findAny();

        if (monitor.isEmpty()) {
            LOGGER.error("Could not find monitor.");
        }

        pawn.configureSplitscreen(monitor.get().getMonitor(), packet.position());
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
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
