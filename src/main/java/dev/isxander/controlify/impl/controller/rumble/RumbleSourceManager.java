/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Merges effect-to-source assignments from assets/controlify/controller/rumble_sources.json.
/// Higher-priority packs override individual effect IDs. The distinct source IDs in
/// the merged assignments define all available sources. Unassigned effects use their
/// caller-specified fallback if available, otherwise master.
public final class RumbleSourceManager implements SimpleControlifyReloadListener<Map<Identifier, RumbleSource>> {
	private static final Identifier DEFINITIONS = CUtil.rl("controller/rumble_sources.json");
	private static final Codec<Map<Identifier, RumbleSource>> CODEC = Codec.unboundedMap(Identifier.CODEC, RumbleSource.CODEC);

	private volatile Map<Identifier, RumbleSource> sourcesByEffect = Map.of();

	public RumbleSource getSource(Identifier effectId, RumbleSource fallback) {
		Map<Identifier, RumbleSource> assignments = this.sourcesByEffect;
		RumbleSource assigned = assignments.get(effectId);
		if (assigned != null) return assigned;
		return assignments.containsValue(fallback) ? fallback : RumbleSource.MASTER;
	}

	/// Every distinct source named in the loaded assignments, including custom namespaces.
	/// No built-in list is added; removing assignments also removes otherwise unused sources.
	public List<RumbleSource> sources() {
		return this.sourcesByEffect.values().stream()
				.distinct()
				.sorted(Comparator.comparing(source -> source.id().toString()))
				.toList();
	}

	@Override
	public CompletableFuture<Map<Identifier, RumbleSource>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, RumbleSource> assignments = new HashMap<>();
			// Resource stacks are ordered from lowest to highest pack priority.
			for (Resource resource : manager.getResourceStack(DEFINITIONS)) {
				try (BufferedReader reader = resource.openAsReader()) {
					CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
							.resultOrPartial(error -> CUtil.LOGGER.error(
									"Failed to parse rumble source assignments from pack '{}': {}",
									resource.sourcePackId(), error
								))
							.ifPresent(assignments::putAll);
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load rumble source assignments from pack '{}'", resource.sourcePackId(), e);
				}
			}
			return Map.copyOf(assignments);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, RumbleSource> data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.sourcesByEffect = Map.copyOf(data), executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("rumble_sources");
	}
}
