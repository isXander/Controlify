package dev.isxander.controlify.platform.client;

import dev.isxander.controlify.platform.client.events.*;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface PlatformClientUtilImpl {
    void registerClientTickStarted(TickEvent event);

    void registerClientTickEnded(TickEvent event);

    void registerClientStopping(LifecycleEvent event);

    void registerClientDisconnected(DisconnectedEvent event);

    void registerAssetReloadListener(ControlifyReloadListener reloadListener);

    void registerBuiltinResourcePack(ResourceLocation id, Component displayName);

    void addHudLayer(RenderLayer renderLayer);
}
