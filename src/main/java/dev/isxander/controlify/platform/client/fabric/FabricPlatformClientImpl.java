package dev.isxander.controlify.platform.client.fabric;

import dev.isxander.controlify.platform.client.PlatformClientUtilImpl;
import dev.isxander.controlify.platform.client.events.DisconnectedEvent;
import dev.isxander.controlify.platform.client.events.LifecycleEvent;
import dev.isxander.controlify.platform.client.events.TickEvent;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import dev.isxander.controlify.platform.fabric.mixins.KeyBindingRegistryImplAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.Collection;

public class FabricPlatformClientImpl implements PlatformClientUtilImpl {
    @Override
    public void registerClientTickStarted(TickEvent event) {
        ClientTickEvents.START_CLIENT_TICK.register(event::onTick);
    }

    @Override
    public void registerClientTickEnded(TickEvent event) {
        ClientTickEvents.END_CLIENT_TICK.register(event::onTick);
    }

    @Override
    public void registerClientStopping(LifecycleEvent event) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(event::onLifecycle);
    }

    @Override
    public void registerClientDisconnected(DisconnectedEvent event) {
        ClientPlayConnectionEvents.DISCONNECT.register(event::onDisconnected);
    }

    @Override
    public void registerAssetReloadListener(ControlifyReloadListener reloadListener) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(reloadListener);
    }

    @Override
    public void registerBuiltinResourcePack(ResourceLocation id, Component displayName) {
        ResourceManagerHelper.registerBuiltinResourcePack(
                id,
                FabricLoader.getInstance().getModContainer("controlify").orElseThrow(),
                displayName,
                ResourcePackActivationType.NORMAL
        );
    }

    @Override
    public void addHudLayer(RenderLayer renderLayer) {
        HudRenderCallback.EVENT.register(renderLayer::render);
    }

    @Override
    public Collection<KeyMapping> getModdedKeyMappings() {
        return KeyBindingRegistryImplAccessor.getCustomKeys();
    }
}
