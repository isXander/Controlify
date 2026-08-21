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
import dev.isxander.controlify.api.contextual.*;
import dev.isxander.controlify.controller.dualsense.TriggerEffectInstanceImpl;
import dev.isxander.controlify.gui.guide.GuideInstanceImpl;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.util.RegistryContextSwapper;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ContextualDomainImpl<C extends Context> implements ContextualDomain<C>, SimpleControlifyReloadListener<ContextualDomainImpl.Preparations> {
	public static final String DIRECTORY = "contextual/facts";
	private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

	private final Identifier id;
	private final CacheSlot<ClientLevel, FactDependencyGraph> resolvedFactGraphs = new CacheSlot<>(this::resolveFactGraph);
	private ContextualStateContributor<C> contributor = ContextualStateContributor.noop();
	private volatile Preparations preparations = Preparations.EMPTY;
	private FactDependencyGraph factGraphWithoutLevel = FactDependencyGraph.empty();

	public ContextualDomainImpl(Identifier id) {
		this.id = Objects.requireNonNull(id, "id");
		this.registerContributor(ContextualStateContributors.COMMON);
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	@Override
	public void registerContributor(ContextualStateContributor<? super C> contributor) {
		Objects.requireNonNull(contributor, "contributor");

		this.contributor = this.contributor.andThen(contributor);
	}

	@Override
	public GuideInstance<C> createGuideInstance(Font font) {
		RuleSetManager<GuideRule.Key, GuideRule> ruleSetManager = RuleSetManager.GUIDE_RULES;

		return new GuideInstanceImpl<>(this, () -> this.requireRuleEngine(ruleSetManager, "guide"), font);
	}

	@Override
	public TriggerEffectInstance<C> createTriggerEffectInstance() {
		RuleSetManager<Identifier, TriggerEffectRule> ruleSetManager = RuleSetManager.TRIGGER_EFFECT_RULES;

		return new TriggerEffectInstanceImpl<>(this, () -> this.requireRuleEngine(ruleSetManager, "trigger effect"));
	}

	private <K, R extends Rule<K>> RuleEngine<K, R> requireRuleEngine(RuleSetManager<K, R> ruleSetManager, String type) {
		return ruleSetManager.getRuleEngine(Minecraft.getInstance().level, this.id)
				.orElseThrow(() -> new IllegalCallerException("This contextual domain has no " + type + " rules associated with it."));
	}

	@Override
	public CompletableFuture<ContextualDomainImpl.Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			List<Resource> resources = manager.getResourceStack(converter.idToFile(this.id));
			var lookup = new PlaceholderLookupProvider(RegistryAccess.EMPTY);
			var ops = lookup.createSerializationContext(JsonOps.INSTANCE);

			var factsMap = new HashMap<Identifier, FactDefinition>();
			for (Resource resource : resources) {
				try (BufferedReader reader = resource.openAsReader()) {
					JsonElement json = JsonParser.parseReader(reader);
					DataResult<List<FactDefinition>> result = FactDefinition.LIST_CODEC.parse(ops, json);
					var facts = result.getOrThrow(error -> new IllegalArgumentException("Failed to parse contextual facts: " + error));

					for (FactDefinition fact : facts) {
						factsMap.remove(fact.id());
						factsMap.put(fact.id(), fact);
					}
				} catch (Exception e) {
					CUtil.LOGGER.error(
							"Failed to load contextual facts for domain '{}' from pack '{}'; skipping this layer",
							e,
							this.id,
							resource.sourcePackId()
					);
				}
			}

			List<FactDefinition> facts = List.copyOf(factsMap.values());
			RegistryContextSwapper registrySwapper = lookup.hasRegisteredPlaceholders()
					? lookup.createSwapper()
					: null;
			Preparations preparations = new Preparations(facts, FactDependencyGraph.empty(), registrySwapper);
			try {
				FactDependencyGraph factGraph = resolveFactGraph(
						preparations,
						ClientRegistryLayer.createRegistryAccess().compositeAccess(),
						false
				);
				return new Preparations(facts, factGraph, registrySwapper);
			} catch (Exception e) {
				CUtil.LOGGER.error(
						"Failed to compile contextual facts for domain '{}'; keeping the previous definitions",
						e,
						this.id
				);
				return this.preparations;
			}
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.apply(data), executor);
	}

	private synchronized void apply(Preparations data) {
		this.preparations = data;
		this.factGraphWithoutLevel = data.factGraphWithoutLevel();
		this.resolvedFactGraphs.clear();
	}

	public synchronized void invalidateResolvedFactGraph() {
		this.resolvedFactGraphs.clear();
	}

	private FactDependencyGraph resolveFactGraph(ClientLevel level) {
		Preparations preparations = this.preparations;
		try {
			return resolveFactGraph(preparations, level.registryAccess(), true);
		} catch (Exception e) {
			CUtil.LOGGER.error(
					"Failed to resolve contextual facts for domain '{}'; using client-static definitions",
					e,
					this.id
			);
			return preparations.factGraphWithoutLevel();
		}
	}

	private FactDependencyGraph resolveFactGraph(
			Preparations preparations,
			HolderLookup.Provider registries,
			boolean logFailures
	) {
		if (preparations.registrySwapper() == null) {
			return FactDependencyGraph.compile(preparations.facts());
		}

		var resolvedFacts = new ArrayList<FactDefinition>(preparations.facts().size());
		for (FactDefinition fact : preparations.facts()) {
			try {
				ContextualPredicate predicate = preparations.registrySwapper()
						.swapTo(ContextualPredicate.CODEC, fact.predicate(), registries)
						.getOrThrow();
				resolvedFacts.add(new FactDefinition(fact.id(), predicate));
			} catch (Exception e) {
				if (logFailures) {
					CUtil.LOGGER.warn(
							"Failed to resolve contextual fact '{}' for domain '{}'; leaving its registry references unresolved",
							e,
							fact.id(),
							this.id
					);
				}
				resolvedFacts.add(fact);
			}
		}

		return FactDependencyGraph.compile(resolvedFacts);
	}

	private synchronized FactDependencyGraph currentFactGraph(@Nullable ClientLevel level) {
		return level == null ? this.factGraphWithoutLevel : this.resolvedFactGraphs.compute(level);
	}

	@Override
	public Identifier getReloadId() {
		return this.id.withPrefix("reload/");
	}

	public ContextualState calculateState(C context, @Nullable Collection<Identifier> requiredFacts) {
		Objects.requireNonNull(context, "context");

		ContextualStateAccumulator state = new ContextualStateAccumulator();
		this.contributor.contribute(context, state);
		ContextualDebug.logStateStart(this.id, state.snapshot(), requiredFacts);

		FactDependencyGraph graph = this.currentFactGraph(Minecraft.getInstance().level);
		ContextualState resolvedState = graph.evaluate(state, requiredFacts, this.id);
		ContextualDebug.logStateComplete(this.id, resolvedState);
		return resolvedState;
	}

	public record Preparations(
			List<FactDefinition> facts,
			FactDependencyGraph factGraphWithoutLevel,
			@Nullable RegistryContextSwapper registrySwapper
	) {
		private static final Preparations EMPTY = new Preparations(List.of(), FactDependencyGraph.empty(), null);
	}
}
