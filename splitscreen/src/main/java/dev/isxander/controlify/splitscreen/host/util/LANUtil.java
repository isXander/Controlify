package dev.isxander.controlify.splitscreen.host.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

import java.net.InetAddress;
import java.util.Optional;

public class LANUtil {
    public static Optional<ServerAddress> getOrPublishLANServer() {
        return Optional.ofNullable(Minecraft.getInstance().getSingleplayerServer())
                .map(LANUtil::getOrPublishLANServer);
    }

    public static ServerAddress getOrPublishLANServer(IntegratedServer server) {
        return new ServerAddress(getLANServerBindAddress().getHostAddress(), getPortOrPublishServer(server));
    }

    // TODO: make this a setting to allow regular LAN play
    public static InetAddress getLANServerBindAddress() {
        return InetAddress.getLoopbackAddress();
    }

    public static int getPortOrPublishServer(IntegratedServer server) {
        int port;
        if (!server.isPublished()) {
            port = HttpUtil.getAvailablePort();
            server.publishServer(GameType.DEFAULT_MODE, false, port);
        } else {
            port = server.getPort();
        }
        return port;
    }
}
