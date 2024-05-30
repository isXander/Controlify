package dev.isxander.controlify.platform.client;

import dev.isxander.controlify.platform.client.events.DisconnectedEvent;
import dev.isxander.controlify.platform.client.events.LifecycleEvent;
import dev.isxander.controlify.platform.client.events.ScreenRenderEvent;
import dev.isxander.controlify.platform.client.events.TickEvent;
import dev.isxander.controlify.platform.client.fabric.FabricPlatformClientImpl;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;

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

    public static void registerPostScreenRender(ScreenRenderEvent event) {
        IMPL.registerPostScreenRender(event);
    }

    public static void addHudLayer(RenderLayer layer) {
        IMPL.addHudLayer(layer);
    }

    public static Collection<KeyMapping> getModdedKeyMappings() {
        return IMPL.getModdedKeyMappings();
    }

    public static <I, O> void setupClientsideHandshake(
            ResourceLocation handshakeId,
            ControlifyPacketCodec<I> clientBoundCodec,
            ControlifyPacketCodec<O> serverBoundCodec,
            Function<I, O> handshakeHandler
    ) {
        IMPL.setupClientsideHandshake(handshakeId, clientBoundCodec, serverBoundCodec, handshakeHandler);
    }

    public static CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen) {
        return IMPL.createCreativeTabHelper(creativeScreen);
    }

    private PlatformClientUtil() {
    }
}
