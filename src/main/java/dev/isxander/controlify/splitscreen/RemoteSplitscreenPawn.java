package dev.isxander.controlify.splitscreen;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import dev.isxander.controlify.mixins.feature.splitscreen.WindowAccessor;
import dev.isxander.controlify.splitscreen.protocol.packets.play.PawnboundConfigureSplitscreenPacket;
import dev.isxander.controlify.splitscreen.protocol.packets.play.PawnboundJoinMyServer;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

public class RemoteSplitscreenPawn implements SplitscreenPawn {
    private final Connection connection;

    public RemoteSplitscreenPawn(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void joinMyServer(int port) {
        this.connection.send(new PawnboundJoinMyServer(port));
    }

    @SuppressWarnings("UnreachableCode")
    @Override
    public void configureSplitscreen(long monitorIndex, SplitscreenPosition position) {
        Minecraft minecraft = Minecraft.getInstance();
        ScreenManager screenManager = ((WindowAccessor) (Object) minecraft.getWindow()).getScreenManager();
        Monitor monitor = screenManager.getMonitor(monitorIndex);

        this.connection.send(new PawnboundConfigureSplitscreenPacket(monitor.getX(), monitor.getY(), position));
    }
}
