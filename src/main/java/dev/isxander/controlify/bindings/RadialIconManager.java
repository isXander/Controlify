/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class RadialIconManager implements SimpleControlifyReloadListener<Map<Identifier, RadialIcon>> {
	public static final RadialIconManager INSTANCE = new RadialIconManager();
	public static final Identifier EMPTY = CUtil.rl("empty");

	private static final Identifier DEFINITIONS = CUtil.rl("radial_icons.json");
	private static final Codec<Map<Identifier, RadialIcon>> CODEC = Codec.unboundedMap(Identifier.CODEC, RadialIcon.CODEC);
	private static final RadialIcon FALLBACK = RadialIcon.model(Identifier.withDefaultNamespace("book"));

	private volatile Map<Identifier, RadialIcon> iconsByBinding = Map.of();

	public boolean isRadialCandidate(InputBinding binding) {
		return iconsByBinding.containsKey(binding.id())
				|| binding.id().getNamespace().equals(ControlifyBindings.MODDED_BIND_NAMESPACE);
	}

	public RadialIcon getIcon(InputBinding binding) {
		return iconsByBinding.getOrDefault(binding.id(), FALLBACK);
	}

	@Override
	public CompletableFuture<Map<Identifier, RadialIcon>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, RadialIcon> mappings = new HashMap<>();
			List<Resource> resources = manager.getResourceStack(DEFINITIONS);

			// Resource stacks are returned from highest to lowest priority.
			// Apply them in reverse so higher packs overwrite individual bindings.
			for (int i = resources.size() - 1; i >= 0; i--) {
				Resource resource = resources.get(i);
				try (BufferedReader reader = resource.openAsReader()) {
					JsonElement json = JsonParser.parseReader(reader);
					CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(error -> CUtil.LOGGER.error(
									"Failed to parse radial icons from pack '{}': {}",
									resource.sourcePackId(),
									error
							))
							.ifPresent(mappings::putAll);
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load radial icons from pack '{}'", resource.sourcePackId(), e);
				}
			}

			return Map.copyOf(mappings);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, RadialIcon> data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> iconsByBinding = data, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("radial_icons");
	}

	private RadialIconManager() {
	}
}
