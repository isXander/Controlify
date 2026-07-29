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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class TriggerEffectRegistry implements SimpleControlifyReloadListener<TriggerEffectRegistry.Preparations> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Identifier USE_ITEM_RESOURCE = CUtil.rl("trigger_effect/use_item.json");
	private static final Identifier SWING_ITEM_RESOURCE = CUtil.rl("trigger_effect/swing_item.json");
	private static final Identifier RELOAD_ID = CUtil.rl("trigger_effect");

	private static final Codec<SerializedRule> SERIALIZED_RULE_CODEC = RecordCodecBuilder.<SerializedRule>create(instance -> instance.group(
		Identifier.CODEC.optionalFieldOf("forItem").forGetter(SerializedRule::forItem),
		Identifier.CODEC.optionalFieldOf("forComponent").forGetter(SerializedRule::forComponent),
		TriggerEffectCodecs.CODEC.fieldOf("effect").forGetter(SerializedRule::effect)
	).apply(instance, SerializedRule::new)).comapFlatMap(
		rule -> rule.forItem().isPresent() == rule.forComponent().isPresent()
			? DataResult.error(() -> "Exactly one of 'forItem' and 'forComponent' must be specified")
			: DataResult.success(rule),
		Function.identity()
	);

	private List<Rule> useItemResourceRules = List.of();
	private List<Rule> swingItemResourceRules = List.of();

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

	private List<Rule> loadResourceStack(ResourceManager manager, Identifier id) {
		List<Rule> rules = new ArrayList<>();

		for (Resource resource : manager.getResourceStack(id).reversed()) {
			try (BufferedReader reader = resource.openAsReader()) {
				JsonElement json = JsonParser.parseReader(reader);
				List<SerializedRule> serializedRules = SERIALIZED_RULE_CODEC.listOf()
					.parse(JsonOps.INSTANCE, json)
					.getOrThrow();

				List<Rule> layerRules = new ArrayList<>(serializedRules.size());
				for (SerializedRule serializedRule : serializedRules) {
					layerRules.add(this.resolveRule(serializedRule));
				}
				rules.addAll(layerRules);
			} catch (Exception e) {
				LOGGER.error(
					"Failed to load adaptive trigger effects from {} in pack {}; skipping this layer",
					id,
					resource.sourcePackId(),
					e
				);
			}
		}

		return List.copyOf(rules);
	}

	private Rule resolveRule(SerializedRule rule) {
		if (rule.forItem().isPresent()) {
			Identifier itemId = rule.forItem().orElseThrow();
			if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
				throw new IllegalArgumentException("Unknown item '" + itemId + "'");
			}
			Item item = BuiltInRegistries.ITEM.getValue(itemId);
			return new ItemRule(item, rule.effect());
		}

		Identifier componentId = rule.forComponent().orElseThrow();
		if (!BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(componentId)) {
			throw new IllegalArgumentException("Unknown data component type '" + componentId + "'");
		}
		DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentId);
		return new ComponentRule(componentType, rule.effect());
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.useItemResourceRules = data.useItemRules;
			this.swingItemResourceRules = data.swingItemRules;
			LOGGER.info(
				"Loaded {} use-item and {} swing-item adaptive trigger effect rules",
				this.useItemResourceRules.size(),
				this.swingItemResourceRules.size()
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
		return findEffect(
			stack,
			this.useItemResourceRules,
			this.useItemComponentEffects,
			this.useItemEffects
		);
	}

	public Optional<DualsenseTriggerEffect> getSwingItemEffect(ItemStack stack) {
		return findEffect(
			stack,
			this.swingItemResourceRules,
			this.swingItemComponentEffects,
			this.swingItemEffects
		);
	}

	private static Optional<DualsenseTriggerEffect> findEffect(
		ItemStack stack,
		List<Rule> resourceRules,
		Map<DataComponentType<?>, Function<Object, DualsenseTriggerEffect>> componentEffects,
		Map<Item, DualsenseTriggerEffect> itemEffects
	) {
		for (Rule rule : resourceRules) {
			if (rule.matches(stack)) {
				return Optional.of(rule.effect());
			}
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

	private record SerializedRule(
		Optional<Identifier> forItem,
		Optional<Identifier> forComponent,
		DualsenseTriggerEffect effect
	) {
	}

	private sealed interface Rule permits ItemRule, ComponentRule {
		boolean matches(ItemStack stack);

		DualsenseTriggerEffect effect();
	}

	private record ItemRule(Item item, DualsenseTriggerEffect effect) implements Rule {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.getItem() == this.item;
		}
	}

	private record ComponentRule(
		DataComponentType<?> componentType,
		DualsenseTriggerEffect effect
	) implements Rule {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.has(this.componentType);
		}
	}

	public static final class Preparations {
		private final List<Rule> useItemRules;
		private final List<Rule> swingItemRules;

		private Preparations(List<Rule> useItemRules, List<Rule> swingItemRules) {
			this.useItemRules = useItemRules;
			this.swingItemRules = swingItemRules;
		}
	}
}
