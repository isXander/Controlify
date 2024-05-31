package dev.isxander.controlify.platform.main.events;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface PlayerJoinedEvent {
    void onInit(ServerPlayer player);
}
