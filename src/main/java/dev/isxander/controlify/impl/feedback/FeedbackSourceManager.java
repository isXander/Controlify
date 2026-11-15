/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Loads which feedback events each source controls from
/// `assets/controlify/controller/feedback_sources.json`:
///
/// ```json
/// { "controlify:world": ["controlify:explosion", "controlify:lightning"] }
/// ```
///
/// Every pack's file is merged. A higher-priority pack that lists an event under a different
/// source moves it there. Every source named in any pack gets a strength slider, even if it
/// lists no events. An event that no source lists is controlled by the master strength only.
public final class FeedbackSourceManager implements SimpleControlifyReloadListener<FeedbackSourceManager.Sources> {
	private static final Identifier DEFINITIONS = CUtil.rl("controller/feedback_sources.json");
	static final Codec<Map<FeedbackSource, List<Identifier>>> CODEC =
			Codec.unboundedMap(FeedbackSource.CODEC, Identifier.CODEC.listOf());

	private volatile Sources sources = new Sources(Map.of(), Set.of());

	/// @param byEvent the source each event (or directly played effect) belongs to
	/// @param all     every source any pack named
	public record Sources(Map<Identifier, FeedbackSource> byEvent, Set<FeedbackSource> all) {
	}

	/// The source that controls `eventId`: the one that lists it, else `fallback` if that
	/// source exists, else master.
	public FeedbackSource getSource(Identifier eventId, FeedbackSource fallback) {
		Sources sources = this.sources;
		FeedbackSource assigned = sources.byEvent().get(eventId);
		if (assigned != null) {
			return assigned;
		}
		return sources.all().contains(fallback) ? fallback : FeedbackSource.MASTER;
	}

	/// The source that lists this event, if any.
	public Optional<FeedbackSource> findAssigned(Identifier eventId) {
		return Optional.ofNullable(this.sources.byEvent().get(eventId));
	}

	/// Every source named by any pack, including custom namespaces.
	public List<FeedbackSource> sources() {
		return this.sources.all().stream()
				.sorted(Comparator.comparing(source -> source.id().toString()))
				.toList();
	}

	@Override
	public CompletableFuture<Sources> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, FeedbackSource> byEvent = new HashMap<>();
			Set<FeedbackSource> all = new LinkedHashSet<>();

			// Resource stacks are ordered from lowest to highest pack priority.
			for (Resource resource : manager.getResourceStack(DEFINITIONS)) {
				try (BufferedReader reader = resource.openAsReader()) {
					CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
							.resultOrPartial(error -> CUtil.LOGGER.error(
									"Failed to parse feedback sources from pack '{}': {}",
									resource.sourcePackId(), error
							))
							.ifPresent(parsed -> parsed.forEach((source, events) -> {
								all.add(source);
								events.forEach(event -> byEvent.put(event, source));
							}));
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load feedback sources from pack '{}'", resource.sourcePackId(), e);
				}
			}

			return new Sources(Map.copyOf(byEvent), Set.copyOf(all));
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Sources data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.sources = data, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("feedback_sources");
	}
}
