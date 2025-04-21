package dev.isxander.controlify.splitscreen.client;

import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class ClientSplitscreenPawn implements SplitscreenPawn {
    private final Minecraft minecraft;

    public ClientSplitscreenPawn(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void joinServer(String host, int port) {
        minecraft.execute(() -> {
            String ip = host + ":" + port;
            var address = new ServerAddress(ip, port);
            var data = new ServerData("Splitscreen Master", ip, ServerData.Type.LAN);

            ConnectScreen.startConnecting(minecraft.screen, minecraft, address, data, false, null);
        });
    }

    @Override
    public void configureWindow(long parentWindowHandle, SplitscreenPosition position) {

    }
}
