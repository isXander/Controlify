package dev.isxander.controlify.splitscreen.client;

import dev.isxander.controlify.splitscreen.ControllerBridge;
import dev.isxander.controlify.splitscreen.protocol.controllerbound.play.ControllerboundGiveMeFocusIfForegroundPacket;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

public class RemoteControllerBridge implements ControllerBridge {

    private final Minecraft minecraft;
    private final Connection connection;

    public RemoteControllerBridge(Minecraft minecraft, Connection connection) {
        this.minecraft = minecraft;
        this.connection = connection;
    }

    @Override
    public void giveFocusToMeIfForeground() {
        this.connection.send(new ControllerboundGiveMeFocusIfForegroundPacket(
                WindowManager.get().getNativeWindowHandle(
                        this.minecraft.getWindow().getWindow()
                )
        ));
    }
}
