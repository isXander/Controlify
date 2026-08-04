/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fabric.platform.client;

import dev.isxander.controlify.platform.client.CreativeTabHelper;
import dev.isxander.controlify.platform.client.HudRenderLayer;
import dev.isxander.controlify.platform.client.PlatformClientUtilImpl;
import dev.isxander.controlify.platform.client.events.DisconnectedEvent;
import dev.isxander.controlify.platform.client.events.LifecycleEvent;
import dev.isxander.controlify.platform.client.events.ScreenRenderEvent;
import dev.isxander.controlify.platform.client.events.TickEvent;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import dev.isxander.controlify.fabric.mixins.KeyBindingRegistryImplAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

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
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			event.onDisconnected(client);
		});
	}

	@Override
	public void registerClientTagsUpdated(LifecycleEvent event) {
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			if (client) {
				event.onLifecycle(Minecraft.getInstance());
			}
		});
	}

	@Override
	public void registerAssetReloadListener(ControlifyReloadListener reloadListener) {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(reloadListener.getReloadId(), reloadListener);
	}

	@Override
	public void registerBuiltinResourcePack(Identifier id, Component displayName) {
		ResourceLoader.registerBuiltinPack(
				id,
				FabricLoader.getInstance().getModContainer("controlify").orElseThrow(),
				displayName,
				PackActivationType.NORMAL
		);
	}

	@Override
	public void registerPostScreenRender(ScreenRenderEvent event) {
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.afterExtract(screen).register((unused, graphics, mouseX, mouseY, tickDelta) -> {
				event.onRender(screen, graphics, mouseX, mouseY, tickDelta);
			});
		});
	}

	@Override
	public void addHudLayer(Identifier id, HudRenderLayer renderLayer) {
		HudElementRegistry.addLast(id, renderLayer::render);
	}

	@Override
	public Collection<KeyMapping> getModdedKeyMappings() {
		return KeyBindingRegistryImplAccessor.getCustomKeys();
	}

	@Override
	public <I, O> void setupClientsideHandshake(Identifier handshakeId, StreamCodec<FriendlyByteBuf, I> clientBoundCodec, StreamCodec<FriendlyByteBuf, O> serverBoundCodec, Function<I, O> handshakeHandler) {
		ClientLoginNetworking.registerGlobalReceiver(handshakeId, (client, handler, buf, listenerAdder) -> {
			I decodedInput = clientBoundCodec.decode(buf);
			O decodedOutput = handshakeHandler.apply(decodedInput);

			FriendlyByteBuf encodedOutput = FriendlyByteBufs.create();
			serverBoundCodec.encode(encodedOutput, decodedOutput);

			return CompletableFuture.completedFuture(encodedOutput);
		});
	}

	@Override
	public CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen) {
		return new FAPIApiCreativeTabHelper(creativeScreen);
	}

	@Override
	public @Nullable ScreenRectangle peekScissorStack(GuiGraphicsExtractor graphics) {
		return graphics.scissorStack.peek();
	}

	@Override
	public void submitGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState guiElement) {
		graphics.guiRenderState.addGuiElement(guiElement);
	}

	@Override
	public <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> holder) {
		return ClientTags.isInWithLocalFallback(tagKey, holder);
	}
}
