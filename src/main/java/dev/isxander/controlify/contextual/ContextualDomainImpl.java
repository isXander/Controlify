/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.contextual.api.Context;
import dev.isxander.controlify.contextual.api.ContextualDomain;
import dev.isxander.controlify.contextual.api.ContextualStateContributor;
import dev.isxander.controlify.contextual.api.GuideInstance;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ContextualDomainImpl<C extends Context> implements ContextualDomain<C>, SimpleControlifyReloadListener<ContextualDomainImpl.Preparations> {
	public static final String DIRECTORY = "contextual/facts";
	private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

	private final Identifier id;
	private volatile List<ContextualStateContributor<? super C>> contributors = List.of();
	private volatile FactDependencyGraph factGraph = FactDependencyGraph.empty();

	public ContextualDomainImpl(Identifier id) {
		this.id = Objects.requireNonNull(id, "id");
		this.registerContributor(ContextualStateContributors.COMMON);
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	@Override
	public synchronized void registerContributor(ContextualStateContributor<? super C> contributor) {
		List<ContextualStateContributor<? super C>> replacement = new ArrayList<>(this.contributors);
		replacement.add(Objects.requireNonNull(contributor, "contributor"));
		this.contributors = List.copyOf(replacement);
	}

	@Override
	public GuideInstance<C> createGuideInstance(Font font) {
		RuleSetManager<GuideRule.Key, GuideRule> ruleSetManager = null; // TODO: get rule set manager from global state somewhere
		RuleEngine<GuideRule.Key, GuideRule> ruleEngine = ruleSetManager.getRuleEngine(this.id)
				.orElseThrow(() -> new IllegalCallerException("This contextual domain has no guide rules associated with it."));

		return new GuideInstanceImpl<>(this, ruleEngine, font);
	}

	@Override
	public CompletableFuture<ContextualDomainImpl.Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			List<Resource> resources = manager.getResourceStack(converter.idToFile(this.id));

			var factsMap = new HashMap<Identifier, FactDefinition>();
			for (Resource resource : resources) {
				try (BufferedReader reader = resource.openAsReader()) {
					JsonElement json = JsonParser.parseReader(reader);
					DataResult<List<FactDefinition>> result = FactDefinition.LIST_CODEC.parse(JsonOps.INSTANCE, json);
					var facts = result.getOrThrow();

					for (FactDefinition fact : facts) {
						factsMap.remove(fact.id());
						factsMap.put(fact.id(), fact);
					}
				} catch (IOException e) {
					throw new IllegalStateException("Failed to load resource", e);
				}
			}

			var facts = factsMap.values();
			var factGraph = FactDependencyGraph.compile(facts);

			return new Preparations(factGraph);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.factGraph = data.facts();
		}, executor);
	}

	@Override
	public Identifier getReloadId() {
		return this.id.withPrefix("reload/");
	}

	public ContextualState calculateState(C context, @Nullable Collection<Identifier> requiredFacts) {
		Objects.requireNonNull(context, "context");

		ContextualStateAccumulator state = new ContextualStateAccumulator();
		for (ContextualStateContributor<? super C> contributor : this.contributors) {
			contributor.contribute(context, state);
		}

		FactDependencyGraph graph = this.factGraph;
		return graph.evaluate(state, requiredFacts);
	}

	public record Preparations(
			FactDependencyGraph facts
	) {}
}
