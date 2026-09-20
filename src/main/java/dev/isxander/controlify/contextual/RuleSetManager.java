/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RuleSetManager<K, R extends Rule<K>> implements SimpleControlifyReloadListener<RuleSetManager.Preparations<K, R>> {
	public static final RuleSetManager<GuideRule.Key, GuideRule> GUIDE_RULES =
			new RuleSetManager<>(GuideRule.CODEC, "guide");
	public static final RuleSetManager<Identifier, TriggerEffectRule> TRIGGER_EFFECT_RULES =
			new RuleSetManager<>(TriggerEffectRule.CODEC, "trigger_effect");

	private static final String DIRECTORY = "contextual/";

	private final Codec<RuleSet<K, R>> ruleSetCodec;
	private final Codec<R> ruleCodec;
	private final String directory;
	private final FileToIdConverter converter;
	private final CacheSlot<ClientLevel, Map<Identifier, RuleEngine<K, R>>> resolvedRuleEngines =
			new CacheSlot<>(this::resolveRuleEngines);

	private volatile Preparations<K, R> preparations;
	private Map<Identifier, RuleEngine<K, R>> ruleEnginesWithoutLevel;

	public RuleSetManager(Codec<R> ruleCodec, String directory) {
		this.ruleSetCodec = RuleSet.createCodec(ruleCodec);
		this.ruleCodec = ruleCodec;
		this.directory = DIRECTORY + directory;
		this.converter = FileToIdConverter.json(this.directory);
		this.preparations = new Preparations<>(Map.of(), null);
		this.ruleEnginesWithoutLevel = Map.of();
	}

	/// With no level, the engine uses the client-static registry context captured during reload.
	/// References to remote registries remain placeholders until the rules are requested with a level.
	public synchronized Optional<RuleEngine<K, R>> getRuleEngine(@Nullable ClientLevel level, Identifier domainId) {
		Objects.requireNonNull(domainId, "domainId");

		Map<Identifier, RuleEngine<K, R>> ruleEngines = level == null
				? this.ruleEnginesWithoutLevel
				: this.resolvedRuleEngines.compute(level);
		return Optional.ofNullable(ruleEngines.get(domainId));
	}

	@Override
	public CompletableFuture<Preparations<K, R>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, List<Resource>> ruleSetResourcesByDomain = converter.listMatchingResourceStacks(manager);
			var lookup = new PlaceholderLookupProvider(RegistryAccess.EMPTY);
			DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);

			var rulesByDomain = new HashMap<Identifier, List<R>>();
			for (var entry : ruleSetResourcesByDomain.entrySet()) {
				Identifier domainId = converter.fileToId(entry.getKey());
				List<R> rules = parseRuleSets(domainId, entry.getValue(), ops);
				rulesByDomain.put(domainId, rules);
			}

			RegistryContextSwapper registrySwapper = lookup.hasRegisteredPlaceholders()
					? lookup.createSwapper()
					: null;
			return new Preparations<>(Map.copyOf(rulesByDomain), registrySwapper);
		}, executor);
	}

	// highest priority resource pack's first rule should be the head of the resultant list.
	private List<R> parseRuleSets(Identifier domainId, List<Resource> ruleSetResources, DynamicOps<JsonElement> ops) {
		var rules = new ArrayList<R>();

		for (var ruleSetResource : ruleSetResources.reversed()) {
			try (var reader = ruleSetResource.openAsReader()) {
				JsonElement json = JsonParser.parseReader(reader);
				DataResult<RuleSet<K, R>> ruleSetResult = this.ruleSetCodec.parse(ops, json);
				RuleSet<K, R> ruleSet = ruleSetResult.getOrThrow();

				rules.addAll(ruleSet.rules());

				// if this rule set is a replace, then anything below it can be ignored
				if (ruleSet.replace()) {
					break;
				}
			} catch (Exception e) {
				CUtil.LOGGER.error(
						"Failed to load contextual {} rules for domain '{}' from pack '{}'; skipping this layer",
						e,
						this.directory,
						domainId,
						ruleSetResource.sourcePackId()
				);
			}
		}

		return List.copyOf(rules);
	}

	private Map<Identifier, RuleEngine<K, R>> resolveRuleEngines(ClientLevel level) {
		return resolveRuleEngines(this.preparations, level.registryAccess(), true);
	}

	private Map<Identifier, RuleEngine<K, R>> resolveRuleEngines(
			Preparations<K, R> preparations,
			HolderLookup.Provider registries,
			boolean logFailures
	) {
		if (preparations.registrySwapper() == null) {
			return createRuleEngines(preparations.rulesByDomain());
		}

		var resolvedRulesByDomain = new HashMap<Identifier, List<R>>(preparations.rulesByDomain().size());
		for (var entry : preparations.rulesByDomain().entrySet()) {
			var resolvedRules = new ArrayList<R>(entry.getValue().size());
			for (R rule : entry.getValue()) {
				try {
					resolvedRules.add(preparations.registrySwapper()
							.swapTo(this.ruleCodec, rule, registries)
							.getOrThrow());
				} catch (Exception e) {
					if (logFailures) {
						CUtil.LOGGER.warn(
								"Failed to resolve a contextual rule for domain '{}'; leaving its registry references unresolved",
								e,
								entry.getKey()
						);
					}
					resolvedRules.add(rule);
				}
			}
			resolvedRulesByDomain.put(entry.getKey(), List.copyOf(resolvedRules));
		}

		return createRuleEngines(resolvedRulesByDomain);
	}

	private Map<Identifier, RuleEngine<K, R>> createRuleEngines(Map<Identifier, List<R>> rulesByDomain) {
		var ruleEngines = new HashMap<Identifier, RuleEngine<K, R>>(rulesByDomain.size());
		rulesByDomain.forEach((domainId, rules) -> ruleEngines.put(domainId, new RuleEngine<>(rules)));
		return Map.copyOf(ruleEngines);
	}

	@Override
	public CompletableFuture<Void> apply(Preparations<K, R> data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.apply(data), executor);
	}

	private synchronized void apply(Preparations<K, R> data) {
		this.preparations = data;
		this.invalidateResolvedRuleEngines();
	}

	public synchronized void invalidateResolvedRuleEngines() {
		this.ruleEnginesWithoutLevel = resolveRuleEngines(
				this.preparations,
				ClientRegistryLayer.createRegistryAccess().compositeAccess(),
				false
		);
		this.resolvedRuleEngines.clear();
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("reload/" + this.directory);
	}

	public record Preparations<K, R extends Rule<K>>(
			Map<Identifier, List<R>> rulesByDomain,
			@Nullable RegistryContextSwapper registrySwapper
	) {}
}
