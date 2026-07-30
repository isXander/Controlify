/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform.client;

import dev.isxander.controlify.platform.client.events.*;
import dev.isxander.controlify.platform.client.resource.ControlifyReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public interface PlatformClientUtilImpl {
	void registerClientTickStarted(TickEvent event);

	void registerClientTickEnded(TickEvent event);

	void registerClientStopping(LifecycleEvent event);

	void registerClientDisconnected(DisconnectedEvent event);

	void registerClientTagsUpdated(LifecycleEvent event);

	void registerAssetReloadListener(ControlifyReloadListener reloadListener);

	void registerBuiltinResourcePack(Identifier id, Component displayName);

	void addHudLayer(Identifier id, HudRenderLayer renderLayer);

	void registerPostScreenRender(ScreenRenderEvent event);

	Collection<KeyMapping> getModdedKeyMappings();

	<I, O> void setupClientsideHandshake(
			Identifier handshakeId,
			StreamCodec<FriendlyByteBuf, I> clientBoundCodec,
			StreamCodec<FriendlyByteBuf, O> serverBoundCodec,
			Function<I, O> handshakeHandler
	);

	CreativeTabHelper createCreativeTabHelper(CreativeModeInventoryScreen creativeScreen);

	@Nullable ScreenRectangle peekScissorStack(GuiGraphicsExtractor graphics);

	void submitGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState guiElement);
}
