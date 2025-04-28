package dev.isxander.controlify.splitscreen.remote;

import dev.isxander.controlify.splitscreen.ControllerBridge;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundGiveMeFocusIfForegroundPacket;
import dev.isxander.controlify.splitscreen.ipc.packets.controllerbound.play.ControllerboundSignalReadyPacket;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

/**
 * A bridge to communicate with the controller client from a remote client.
 * This is used when the client is not the controller.
 * It communicates via packets.
 */
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

    @Override
    public void signalImReady(boolean finished, float progress) {
        this.connection.send(new ControllerboundSignalReadyPacket(finished, progress));
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
