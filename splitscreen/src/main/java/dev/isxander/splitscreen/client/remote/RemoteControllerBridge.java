package dev.isxander.splitscreen.client.remote;

import dev.isxander.splitscreen.client.ControllerBridge;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundEngineCustomPayloadPacket;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundGiveMeFocusIfForegroundPacket;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundServerDisconnectedPacket;
import dev.isxander.splitscreen.client.ipc.packets.controllerbound.play.ControllerboundSignalReadyPacket;
import dev.isxander.splitscreen.client.engine.impl.reparenting.wm.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
    public void serverDisconnected(Component reason) {
        this.connection.send(new ControllerboundServerDisconnectedPacket(reason));
    }

    public void sendEnginePayload(CustomPacketPayload payload) {
        this.connection.send(new ControllerboundEngineCustomPayloadPacket(payload));
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
