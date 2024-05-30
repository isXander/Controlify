package dev.isxander.controlify.platform.client.fabric;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import dev.isxander.controlify.platform.client.PlatformClientUtilImpl;
import dev.isxander.controlify.platform.client.events.DisconnectedEvent;
import dev.isxander.controlify.platform.client.events.LifecycleEvent;
import dev.isxander.controlify.platform.client.events.ScreenRenderEvent;
import dev.isxander.controlify.platform.client.events.TickEvent;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import dev.isxander.controlify.platform.fabric.mixins.KeyBindingRegistryImplAccessor;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
    public void registerPostScreenRender(ScreenRenderEvent event) {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenEvents.afterRender(screen).register((unused, graphics, mouseX, mouseY, tickDelta) -> {
                event.onRender(screen, graphics, mouseX, mouseY, tickDelta);
            });
        });
    }

    @Override
    public void addHudLayer(RenderLayer renderLayer) {
        HudRenderCallback.EVENT.register(renderLayer::render);
    }

    @Override
    public Collection<KeyMapping> getModdedKeyMappings() {
        return KeyBindingRegistryImplAccessor.getCustomKeys();
    }

    @Override
    public <I, O> void setupClientsideHandshake(ResourceLocation handshakeId, ControlifyPacketCodec<I> clientBoundCodec, ControlifyPacketCodec<O> serverBoundCodec, Function<I, O> handshakeHandler) {
        ClientLoginNetworking.registerGlobalReceiver(handshakeId, (client, handler, buf, listenerAdder) -> {
            I decodedInput = clientBoundCodec.decode(buf);
            O decodedOutput = handshakeHandler.apply(decodedInput);

            FriendlyByteBuf encodedOutput = PacketByteBufs.create();
            serverBoundCodec.encode(encodedOutput, decodedOutput);

            return CompletableFuture.completedFuture(encodedOutput);
        });
    }

    @Override
    public CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen) {
        return new FAPIImplCreativeTabHelper(creativeScreen);
    }
}
