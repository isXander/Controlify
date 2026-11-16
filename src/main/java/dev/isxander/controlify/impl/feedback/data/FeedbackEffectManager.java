/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.impl.feedback.FeedbackRegistryImpl;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Loads data effects from `assets/<namespace>/controller/feedback/<path>.json` into the registry.
/// The effect id is `<namespace>:<path>`. The highest-priority pack wins per id, and a data
/// effect takes precedence over any code effect with the same id.
public final class FeedbackEffectManager implements SimpleControlifyReloadListener<Map<Identifier, DataFeedbackEffect>> {
	private static final FileToIdConverter CONVERTER = FileToIdConverter.json("controller/feedback/effect");

	private final FeedbackRegistryImpl registry;

	public FeedbackEffectManager(FeedbackRegistryImpl registry) {
		this.registry = registry;
	}

	@Override
	public CompletableFuture<Map<Identifier, DataFeedbackEffect>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, DataFeedbackEffect> effects = new HashMap<>();
			for (Map.Entry<Identifier, Resource> entry : CONVERTER.listMatchingResources(manager).entrySet()) {
				Identifier id = CONVERTER.fileToId(entry.getKey());
				Resource resource = entry.getValue();
				try (BufferedReader reader = resource.openAsReader()) {
					DataFeedbackEffect.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
							.resultOrPartial(error -> CUtil.LOGGER.error(
									"Failed to parse feedback effect '{}' from pack '{}': {}",
									id, resource.sourcePackId(), error
							))
							.ifPresent(effect -> effects.put(id, effect));
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load feedback effect '{}' from pack '{}'", id, resource.sourcePackId(), e);
				}
			}
			return Map.copyOf(effects);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, DataFeedbackEffect> data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.registry.setDataEffects(data), executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("feedback_effects");
	}
}
