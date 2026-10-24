/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.impl.action.proxy.ActionProxyManagerImpl;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ActivatorConfigManager implements SimpleControlifyReloadListener<Map<Identifier, ActivatorConfig<?>>> {
	private static final Identifier DEFINITIONS = CUtil.rl("controller/activators.json");
	private static final Codec<Map<Identifier, ActivatorConfig<?>>> CODEC = Codec.unboundedMap(Identifier.CODEC, ActivatorConfig.CODEC);

	private static final ActivatorConfig<?> FALLBACK =
			new CompoundActivator.Config(List.of());
	private static final ActivatorConfig<?> PROXY_ACTIVATOR =
			new CompoundActivator.Config(List.of(HoldLatchActivator.Config.INSTANCE, PressPulseActivator.Config.INSTANCE));

	private volatile Map<Identifier, ActivatorConfig<?>> configsByAction = Map.of();

	public ActivatorConfig<?> getConfig(Identifier actionId) {
		return configsByAction.getOrDefault(
				actionId,
				ActionProxyManagerImpl.INSTANCE.isKeyMappingProxy(actionId) ? PROXY_ACTIVATOR : FALLBACK
		);
	}

	@Override
	public CompletableFuture<Map<Identifier, ActivatorConfig<?>>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, ActivatorConfig<?>> mappings = new HashMap<>();

			// Resource stacks are returned from lowest to highest priority, so later
			// packs overwrite individual actions from earlier packs.
			for (Resource resource : manager.getResourceStack(DEFINITIONS)) {
				try (BufferedReader reader = resource.openAsReader()) {
					JsonElement json = JsonParser.parseReader(reader);
					CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(error -> CUtil.LOGGER.error(
									"Failed to parse activator configs from pack '{}': {}",
									resource.sourcePackId(),
									error
							))
							.ifPresent(mappings::putAll);
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load activator configs from pack '{}'", resource.sourcePackId(), e);
				}
			}

			return Map.copyOf(mappings);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, ActivatorConfig<?>> data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			configsByAction = data;
			Controlify.instance().getControllerManager().ifPresent(controllerManager -> {
				for (var controller : controllerManager.getAllControllers()) {
					controller.input().reloadActions();
				}
			});
		}, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("activators");
	}
}
