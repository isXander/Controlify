/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
//? if >=26.2 {
import net.minecraft.advancements.predicates.ItemPredicate;
//?} else {
/*import net.minecraft.advancements.criterion.ItemPredicate;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class TriggerEffectRegistry implements SimpleControlifyReloadListener<TriggerEffectRegistry.Preparations> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Identifier USE_ITEM_RESOURCE = CUtil.rl("trigger_effect/use_item.json");
	private static final Identifier SWING_ITEM_RESOURCE = CUtil.rl("trigger_effect/swing_item.json");
	private static final Identifier RELOAD_ID = CUtil.rl("trigger_effect");

	private static final Codec<Rule> RULE_CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			ItemPredicate.CODEC.fieldOf("when").forGetter(Rule::when),
			TriggerEffectCodecs.CODEC.fieldOf("effect").forGetter(Rule::effect)
		).apply(instance, Rule::new));
	private static final Codec<List<Rule>> RULES_CODEC = RULE_CODEC.listOf();

	private List<PreparedLayer> useItemResourceLayers = List.of();
	private List<PreparedLayer> swingItemResourceLayers = List.of();
	private final CacheSlot<ClientLevel, ResolvedRules> resolvedRules = new CacheSlot<>(this::resolveResourceRules);

	private final Map<ItemStack, CachedResourceMatch> useItemResourceCache = new WeakHashMap<>();
	private final Map<ItemStack, CachedResourceMatch> swingItemResourceCache = new WeakHashMap<>();

	private final Map<DataComponentType<?>, Function<Object, DualsenseTriggerEffect>> useItemComponentEffects = new LinkedHashMap<>();
	private final Map<DataComponentType<?>, Function<Object, DualsenseTriggerEffect>> swingItemComponentEffects = new LinkedHashMap<>();
	private final Map<Item, DualsenseTriggerEffect> useItemEffects = new LinkedHashMap<>();
	private final Map<Item, DualsenseTriggerEffect> swingItemEffects = new LinkedHashMap<>();

	@Override
	public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> new Preparations(
			this.loadResourceStack(manager, USE_ITEM_RESOURCE),
			this.loadResourceStack(manager, SWING_ITEM_RESOURCE)
		), executor);
	}

	private List<PreparedLayer> loadResourceStack(ResourceManager manager, Identifier id) {
		List<PreparedLayer> layers = new ArrayList<>();

		for (Resource resource : manager.getResourceStack(id).reversed()) {
			try (BufferedReader reader = resource.openAsReader()) {
				PlaceholderLookupProvider lookup = new PlaceholderLookupProvider(RegistryAccess.EMPTY);
				DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
				JsonElement json = JsonParser.parseReader(reader);
				List<Rule> layerRules = RULES_CODEC
					.parse(ops, json)
					.getOrThrow();
				layers.add(new PreparedLayer(
					List.copyOf(layerRules),
					resource.sourcePackId(),
					lookup.hasRegisteredPlaceholders() ? lookup.createSwapper() : null
				));
			} catch (Exception e) {
				LOGGER.error(
					"Failed to load adaptive trigger effects from {} in pack {}; skipping this layer",
					id,
					resource.sourcePackId(),
					e
				);
			}
		}

		return List.copyOf(layers);
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.useItemResourceLayers = data.useItemLayers;
			this.swingItemResourceLayers = data.swingItemLayers;
			this.invalidateResolvedResourceRules();
			LOGGER.info(
				"Loaded {} use-item and {} swing-item adaptive trigger effect rule layers",
				this.useItemResourceLayers.size(),
				this.swingItemResourceLayers.size()
			);
		}, executor);
	}

	public <T> void registerUseItemComponentEffect(
		DataComponentType<T> componentType,
		Function<? super T, DualsenseTriggerEffect> effectFunction
	) {
		this.useItemComponentEffects.put(componentType, castEffectFunction(effectFunction));
	}

	public <T> void registerSwingItemComponentEffect(
		DataComponentType<T> componentType,
		Function<? super T, DualsenseTriggerEffect> effectFunction
	) {
		this.swingItemComponentEffects.put(componentType, castEffectFunction(effectFunction));
	}

	public void registerUseItemEffect(Item item, DualsenseTriggerEffect effect) {
		this.useItemEffects.put(item, effect);
	}

	public void registerSwingItemEffect(Item item, DualsenseTriggerEffect effect) {
		this.swingItemEffects.put(item, effect);
	}

	public Optional<DualsenseTriggerEffect> getUseItemEffect(ItemStack stack) {
		ResolvedRules rules = this.currentResourceRules();
		return findEffect(
			stack,
			rules.useItemRules(),
			this.useItemResourceCache,
			this.useItemComponentEffects,
			this.useItemEffects
		);
	}

	public Optional<DualsenseTriggerEffect> getSwingItemEffect(ItemStack stack) {
		ResolvedRules rules = this.currentResourceRules();
		return findEffect(
			stack,
			rules.swingItemRules(),
			this.swingItemResourceCache,
			this.swingItemComponentEffects,
			this.swingItemEffects
		);
	}

	private Optional<DualsenseTriggerEffect> findEffect(
		ItemStack stack,
		List<Rule> resourceRules,
		Map<ItemStack, CachedResourceMatch> resourceCache,
		Map<DataComponentType<?>, Function<Object, DualsenseTriggerEffect>> componentEffects,
		Map<Item, DualsenseTriggerEffect> itemEffects
	) {
		Optional<DualsenseTriggerEffect> resourceEffect = this.findResourceEffect(stack, resourceRules, resourceCache);
		if (resourceEffect.isPresent()) {
			return resourceEffect;
		}

		for (var entry : componentEffects.entrySet()) {
			if (stack.has(entry.getKey())) {
				@Nullable DualsenseTriggerEffect effect = entry.getValue().apply(stack.get(entry.getKey()));
				if (effect != null) {
					return Optional.of(effect);
				}
			}
		}

		return Optional.ofNullable(itemEffects.get(stack.getItem()));
	}

	private Optional<DualsenseTriggerEffect> findResourceEffect(
		ItemStack stack,
		List<Rule> rules,
		Map<ItemStack, CachedResourceMatch> cache
	) {
		CachedResourceMatch cached = cache.get(stack);
		if (cached == null || !ItemStack.matches(stack, cached.snapshot())) {
			Optional<DualsenseTriggerEffect> effect = rules.stream()
				.filter(rule -> rule.matches(stack))
				.map(Rule::effect)
				.findFirst();
			cached = new CachedResourceMatch(stack.copy(), effect);
			cache.put(stack, cached);
		}

		return cached.effect();
	}

	private ResolvedRules currentResourceRules() {
		ClientLevel level = Minecraft.getInstance().level;
		return level == null ? ResolvedRules.EMPTY : this.resolvedRules.compute(level);
	}

	private ResolvedRules resolveResourceRules(ClientLevel level) {
		HolderLookup.Provider registries = level.registryAccess();
		List<Rule> useItemRules = this.resolveResourceLayers(this.useItemResourceLayers, USE_ITEM_RESOURCE, registries);
		List<Rule> swingItemRules = this.resolveResourceLayers(this.swingItemResourceLayers, SWING_ITEM_RESOURCE, registries);
		this.invalidateResourceRuleCaches();
		LOGGER.info(
			"Resolved {} use-item and {} swing-item adaptive trigger effect rules for the current world",
			useItemRules.size(),
			swingItemRules.size()
		);
		return new ResolvedRules(useItemRules, swingItemRules);
	}

	private List<Rule> resolveResourceLayers(
		List<PreparedLayer> layers,
		Identifier id,
		HolderLookup.Provider registries
	) {
		List<Rule> rules = new ArrayList<>();

		for (PreparedLayer layer : layers) {
			try {
				List<Rule> resolvedRules = layer.registrySwapper() == null
					? layer.rules()
					: layer.registrySwapper().swapTo(RULES_CODEC, layer.rules(), registries).getOrThrow();
				rules.addAll(resolvedRules);
			} catch (Exception e) {
				LOGGER.error(
					"Failed to resolve adaptive trigger effects from {} in pack {} for the current world; skipping this layer",
					id,
					layer.sourcePackId(),
					e
				);
			}
		}

		return List.copyOf(rules);
	}

	public void invalidateResolvedResourceRules() {
		this.resolvedRules.clear();
		this.invalidateResourceRuleCaches();
	}

	public void invalidateResourceRuleCaches() {
		this.useItemResourceCache.clear();
		this.swingItemResourceCache.clear();
	}

	@SuppressWarnings("unchecked")
	private static <T> Function<Object, DualsenseTriggerEffect> castEffectFunction(
		Function<? super T, DualsenseTriggerEffect> effectFunction
	) {
		return (Function<Object, DualsenseTriggerEffect>) effectFunction;
	}

	@Override
	public Identifier getReloadId() {
		return RELOAD_ID;
	}

	private record Rule(
		ItemPredicate when,
		DualsenseTriggerEffect effect
	) {
		public boolean matches(ItemStack stack) {
			return this.when.test(stack);
		}
	}

	private record PreparedLayer(
		List<Rule> rules,
		String sourcePackId,
		@Nullable RegistryContextSwapper registrySwapper
	) {
	}

	private record ResolvedRules(
		List<Rule> useItemRules,
		List<Rule> swingItemRules
	) {
		private static final ResolvedRules EMPTY = new ResolvedRules(List.of(), List.of());
	}

	private record CachedResourceMatch(
		ItemStack snapshot,
		Optional<DualsenseTriggerEffect> effect
	) {
	}

	public static final class Preparations {
		private final List<PreparedLayer> useItemLayers;
		private final List<PreparedLayer> swingItemLayers;

		private Preparations(List<PreparedLayer> useItemLayers, List<PreparedLayer> swingItemLayers) {
			this.useItemLayers = useItemLayers;
			this.swingItemLayers = swingItemLayers;
		}
	}
}
