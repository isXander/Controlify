package dev.isxander.controlify.platform.client;

import dev.isxander.controlify.platform.client.events.DisconnectedEvent;
import dev.isxander.controlify.platform.client.events.LifecycleEvent;
import dev.isxander.controlify.platform.client.events.TickEvent;
import dev.isxander.controlify.platform.client.fabric.FabricPlatformClientImpl;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class PlatformClientUtil {
    private static final PlatformClientUtilImpl IMPL = new FabricPlatformClientImpl();

    public static void registerClientTickStarted(TickEvent event) {
        IMPL.registerClientTickStarted(event);
    }

    public static void registerClientTickEnded(TickEvent event) {
        IMPL.registerClientTickEnded(event);
    }

    public static void registerClientStopping(LifecycleEvent event) {
        IMPL.registerClientStopping(event);
    }

    public static void registerClientDisconnected(DisconnectedEvent event) {
        IMPL.registerClientDisconnected(event);
    }

    public static void registerAssetReloadListener(ControlifyReloadListener reloadListener) {
        IMPL.registerAssetReloadListener(reloadListener);
    }

    public static void registerBuiltinResourcePack(ResourceLocation id, Component displayName) {
        IMPL.registerBuiltinResourcePack(id, displayName);
    }

    public static void addHudLayer(RenderLayer layer) {
        IMPL.addHudLayer(layer);
    }

    private PlatformClientUtil() {
    }
}
