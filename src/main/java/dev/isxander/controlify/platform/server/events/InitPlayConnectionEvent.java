package dev.isxander.controlify.platform.server.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@FunctionalInterface
public interface InitPlayConnectionEvent {
    void onInit(ServerGamePacketListenerImpl handler, MinecraftServer server);
}
