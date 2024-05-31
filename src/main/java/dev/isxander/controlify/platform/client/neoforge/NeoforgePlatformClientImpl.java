//? if neoforge {
package dev.isxander.controlify.platform.client.neoforge;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import dev.isxander.controlify.platform.client.PlatformClientUtilImpl;
import dev.isxander.controlify.platform.client.events.DisconnectedEvent;
import dev.isxander.controlify.platform.client.events.LifecycleEvent;
import dev.isxander.controlify.platform.client.events.ScreenRenderEvent;
import dev.isxander.controlify.platform.client.events.TickEvent;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.platform.client.util.RenderLayer;
import dev.isxander.controlify.platform.neoforge.VanillaKeyMappingHolder;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoforgePlatformClientImpl implements PlatformClientUtilImpl {
    private @Nullable Collection<KeyMapping> moddedKeyMappings;

    @Override
    public void registerClientTickStarted(TickEvent event) {
        NeoForge.EVENT_BUS.<ClientTickEvent.Pre>addListener(e -> {
            event.onTick(Minecraft.getInstance());
        });
    }

    @Override
    public void registerClientTickEnded(TickEvent event) {
        NeoForge.EVENT_BUS.<ClientTickEvent.Post>addListener(e -> {
            event.onTick(Minecraft.getInstance());
        });
    }

    @Override
    public void registerClientStopping(LifecycleEvent event) {
        NeoForge.EVENT_BUS.<GameShuttingDownEvent>addListener(e -> {
            event.onLifecycle(Minecraft.getInstance());
        });
    }

    @Override
    public void registerClientDisconnected(DisconnectedEvent event) {
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(e -> {
            event.onDisconnected(Minecraft.getInstance());
        });
    }

    @Override
    public void registerAssetReloadListener(ControlifyReloadListener reloadListener) {
        getModEventBus().<RegisterClientReloadListenersEvent>addListener(e -> {
            e.registerReloadListener(reloadListener);
        });
    }

    @Override
    public void registerBuiltinResourcePack(ResourceLocation id, Component displayName) {
        getModEventBus().<AddPackFindersEvent>addListener(e -> {
            e.addPackFinders(
                    id.withPrefix("resourcepacks/"),
                    PackType.CLIENT_RESOURCES,
                    displayName,
                    PackSource.BUILT_IN,
                    false,
                    Pack.Position.TOP
            );
        });
    }

    @Override
    public void addHudLayer(ResourceLocation id, RenderLayer renderLayer) {
        getModEventBus().<RegisterGuiLayersEvent>addListener(e -> {
            e.registerAboveAll(id, renderLayer);
        });
    }

    @Override
    public void registerPostScreenRender(ScreenRenderEvent event) {
        NeoForge.EVENT_BUS.<ScreenEvent.Render.Post>addListener(e -> {
            event.onRender(e.getScreen(), e.getGuiGraphics(), e.getMouseX(), e.getMouseY(), e.getPartialTick());
        });
    }

    @Override
    public Collection<KeyMapping> getModdedKeyMappings() {
        if (moddedKeyMappings == null)
            moddedKeyMappings = calculateModdedKeyMappings();
        return moddedKeyMappings;
    }

    private Collection<KeyMapping> calculateModdedKeyMappings() {
        Options options = Minecraft.getInstance().options;
        KeyMapping[] vanillaAndModded = options.keyMappings;
        List<KeyMapping> vanillaOnly = Arrays.asList(((VanillaKeyMappingHolder) options).controlify$getVanillaKeys());

        return Arrays.stream(vanillaAndModded)
                .filter(key -> !vanillaOnly.contains(key))
                .toList();
    }

    @Override
    public <I, O> void setupClientsideHandshake(ResourceLocation handshakeId, ControlifyPacketCodec<I> clientBoundCodec, ControlifyPacketCodec<O> serverBoundCodec, Function<I, O> handshakeHandler) {
        // TODO
    }

    @Override
    public CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen) {
        return new NeoforgeCreativeTabHelper(creativeScreen);
    }

    private IEventBus getModEventBus() {
        return ModLoadingContext.get().getActiveContainer().getEventBus();
    }
}
//?}
