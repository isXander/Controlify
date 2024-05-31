package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

@FunctionalInterface
public interface DisconnectedEvent {
    void onDisconnected(Minecraft minecraft);
}
