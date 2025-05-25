package dev.isxander.controlify.platform.client;

import dev.isxander.controlify.platform.client.events.*;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
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

    void addHudLayer(ResourceLocation id, HudRenderLayer renderLayer);

    void registerPostScreenRender(ScreenRenderEvent event);

    Collection<KeyMapping> getModdedKeyMappings();

    <I, O> void setupClientsideHandshake(
            ResourceLocation handshakeId,
            StreamCodec<FriendlyByteBuf, I> clientBoundCodec,
            StreamCodec<FriendlyByteBuf, O> serverBoundCodec,
            Function<I, O> handshakeHandler
    );

    CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen);
}
