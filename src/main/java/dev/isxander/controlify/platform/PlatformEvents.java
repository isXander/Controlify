package dev.isxander.controlify.platform;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PlatformEvents {

    public static void registerClientTickStarted(Consumer<Minecraft> event) {
        ClientTickEvents.START_CLIENT_TICK.register(event::accept);
    }

    public static void invokeClientTickStarted(Minecraft client) {
        ClientTickEvents.START_CLIENT_TICK.invoker().onStartTick(client);
    }

    public static void registerClientTickEnded(Consumer<Minecraft> event) {
        ClientTickEvents.END_CLIENT_TICK.register(event::accept);
    }

    public static void invokeClientTickEnded(Minecraft client) {
        ClientTickEvents.END_CLIENT_TICK.invoker().onEndTick(client);
    }

    public static void registerClientStopping(Consumer<Minecraft> event) {
        ClientTickEvents.END_CLIENT_TICK.register(event::accept);
    }

    public static void invokeClientStopping(Minecraft client) {
        ClientLifecycleEvents.CLIENT_STOPPING.invoker().onClientStopping(client);
    }

    public static void registerClientDisconnected(BiConsumer<ClientPacketListener, Minecraft> event) {
        ClientPlayConnectionEvents.DISCONNECT.register(event::accept);
    }

    public static void invokeClientDisconnected(ClientPacketListener handler, Minecraft client) {
        ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(handler, client);
    }

    private PlatformEvents() {
    }
}
