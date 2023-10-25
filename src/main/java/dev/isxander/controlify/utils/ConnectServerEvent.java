package dev.isxander.controlify.utils;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ConnectServerEvent {
    Event<ConnectServerEvent> EVENT = EventFactory.createArrayBacked(ConnectServerEvent.class, listeners -> (minecraft, address, info) -> {
        for (ConnectServerEvent listener : listeners) {
            listener.onConnect(minecraft, address, info);
        }
    });

    void onConnect(Minecraft minecraft, ServerAddress address, @Nullable ServerData info);
}
