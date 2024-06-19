package dev.isxander.controlify.platform.client;

import dev.isxander.controlify.platform.client.events.*;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;

public interface PlatformClientUtilImpl {
    void registerClientTickStarted(TickEvent event);

    void registerClientTickEnded(TickEvent event);

    void registerClientStopping(LifecycleEvent event);

    void registerClientDisconnected(DisconnectedEvent event);

    void registerAssetReloadListener(ControlifyReloadListener reloadListener);

    void registerBuiltinResourcePack(ResourceLocation id, Component displayName);

    void addHudLayer(ResourceLocation id, RenderLayer renderLayer);

    void registerPostScreenRender(ScreenRenderEvent event);

    Collection<KeyMapping> getModdedKeyMappings();

    <I, O> void setupClientsideHandshake(
            ResourceLocation handshakeId,
            ControlifyPacketCodec<I> clientBoundCodec,
            ControlifyPacketCodec<O> serverBoundCodec,
            Function<I, O> handshakeHandler
    );

    CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen);
}
