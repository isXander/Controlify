/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
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

	private static final Codec<List<String>> ITEM_SELECTORS_CODEC = Codec.either(
		Codec.STRING,
		Codec.STRING.listOf(1, Integer.MAX_VALUE)
	).xmap(
		either -> either.map(List::of, Function.identity()),
		Either::right
	);

	private static final Codec<Tag> NBT_PATTERN_CODEC = Codec.PASSTHROUGH.xmap(
		dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue(),
		tag -> new Dynamic<>(NbtOps.INSTANCE, tag)
	);

	private static final Codec<Map<Identifier, Tag>> COMPONENT_MATCHERS_CODEC = Codec
		.unboundedMap(Identifier.CODEC, NBT_PATTERN_CODEC)
		.validate(matchers -> matchers.isEmpty()
			? DataResult.error(() -> "At least one component matcher must be specified")
			: DataResult.success(matchers));

	private static final Codec<Either<List<Identifier>, Map<Identifier, Tag>>> COMPONENTS_CODEC = Codec.either(
		Identifier.CODEC.listOf(1, Integer.MAX_VALUE),
		COMPONENT_MATCHERS_CODEC
	);

	private static final Codec<SerializedSelector> SERIALIZED_SELECTOR_CODEC =
		RecordCodecBuilder.<SerializedSelector>create(instance -> instance.group(
				ITEM_SELECTORS_CODEC.optionalFieldOf("items", List.of()).forGetter(SerializedSelector::items),
				COMPONENTS_CODEC.optionalFieldOf("components").forGetter(SerializedSelector::components)
			).apply(instance, SerializedSelector::new))
			.validate(selector -> selector.items().isEmpty() && selector.components().isEmpty()
				? DataResult.error(() -> "At least one item or component condition must be specified")
				: DataResult.success(selector));

	private static final Codec<SerializedRule> SERIALIZED_RULE_CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			SERIALIZED_SELECTOR_CODEC.fieldOf("when").forGetter(SerializedRule::when),
			TriggerEffectCodecs.CODEC.fieldOf("effect").forGetter(SerializedRule::effect)
		).apply(instance, SerializedRule::new));

	private List<Rule> useItemResourceRules = List.of();
	private List<Rule> swingItemResourceRules = List.of();

	private final Map<ItemStack, CachedResourceMatches> useItemResourceCache = new WeakHashMap<>();
	private final Map<ItemStack, CachedResourceMatches> swingItemResourceCache = new WeakHashMap<>();

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
		List<ItemCondition> itemConditions = new ArrayList<>(rule.when().items().size());
		for (String itemSelector : rule.when().items()) {
			boolean tag = itemSelector.startsWith("#");
			String idString = tag ? itemSelector.substring(1) : itemSelector;
			Identifier itemId = Identifier.tryParse(idString);
			if (itemId == null) {
				throw new IllegalArgumentException("Invalid item selector '" + itemSelector + "'");
			}

			if (tag) {
				itemConditions.add(new TagItemCondition(TagKey.create(Registries.ITEM, itemId)));
				continue;
			}

			if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
				throw new IllegalArgumentException("Unknown item '" + itemId + "'");
			}
			Item item = BuiltInRegistries.ITEM.getValue(itemId);
			itemConditions.add(new ExactItemCondition(item));
		}

		List<ComponentCondition> componentConditions = rule.when().components()
			.map(components -> components.map(
				ids -> ids.stream().map(this::resolvePresenceComponent).toList(),
				matchers -> matchers.entrySet().stream().map(this::resolveMatchingComponent).toList()
			))
			.orElseGet(List::of);

		return new Rule(
			List.copyOf(itemConditions),
			componentConditions,
			rule.effect()
		);
	}

	private ComponentCondition resolvePresenceComponent(Identifier componentId) {
		return new PresenceComponentCondition(this.resolveComponentType(componentId));
	}

	private ComponentCondition resolveMatchingComponent(Map.Entry<Identifier, Tag> entry) {
		DataComponentType<?> componentType = this.resolveComponentType(entry.getKey());
		if (componentType.isTransient()) {
			throw new IllegalArgumentException("Data component type '" + entry.getKey() + "' is not persistent");
		}
		return new MatchingComponentCondition(componentType, entry.getValue());
	}

	private DataComponentType<?> resolveComponentType(Identifier componentId) {
		if (!BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(componentId)) {
			throw new IllegalArgumentException("Unknown data component type '" + componentId + "'");
		}
		return BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentId);
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.useItemResourceRules = data.useItemRules;
			this.swingItemResourceRules = data.swingItemRules;
			this.invalidateResourceRuleCaches();
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
			this.useItemResourceCache,
			this.useItemComponentEffects,
			this.useItemEffects
		);
	}

	public Optional<DualsenseTriggerEffect> getSwingItemEffect(ItemStack stack) {
		return findEffect(
			stack,
			this.swingItemResourceRules,
			this.swingItemResourceCache,
			this.swingItemComponentEffects,
			this.swingItemEffects
		);
	}

	private Optional<DualsenseTriggerEffect> findEffect(
		ItemStack stack,
		List<Rule> resourceRules,
		Map<ItemStack, CachedResourceMatches> resourceCache,
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
		Map<ItemStack, CachedResourceMatches> cache
	) {
		CachedResourceMatches cached = cache.get(stack);
		if (cached == null || !ItemStack.isSameItemSameComponents(stack, cached.snapshot())) {
			cached = new CachedResourceMatches(
				stack.copy(),
				new IdentityHashMap<>(),
				new IdentityHashMap<>()
			);
			cache.put(stack, cached);
		}

		MatchContext context = new MatchContext(stack, cached);
		return rules.stream()
			.filter(rule -> rule.matches(context))
			.map(Rule::effect)
			.findFirst();
	}

	private static HolderLookup.Provider registryAccess() {
		var connection = Minecraft.getInstance().getConnection();
		return connection != null
			? connection.registryAccess()
			: RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
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

	private record SerializedSelector(
		List<String> items,
		Optional<Either<List<Identifier>, Map<Identifier, Tag>>> components
	) {
	}

	private record SerializedRule(
		SerializedSelector when,
		DualsenseTriggerEffect effect
	) {
	}

	private record Rule(
		List<ItemCondition> itemConditions,
		List<ComponentCondition> componentConditions,
		DualsenseTriggerEffect effect
	) {
		boolean matches(MatchContext context) {
			return (this.itemConditions.isEmpty() || this.itemConditions.stream().anyMatch(condition -> condition.matches(context.stack())))
				&& this.componentConditions.stream().allMatch(condition -> condition.matches(context));
		}
	}

	private sealed interface ItemCondition permits ExactItemCondition, TagItemCondition {
		boolean matches(ItemStack stack);
	}

	private record ExactItemCondition(Item item) implements ItemCondition {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.getItem() == this.item;
		}
	}

	private record TagItemCondition(TagKey<Item> tag) implements ItemCondition {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.is(this.tag);
		}
	}

	private sealed interface ComponentCondition permits PresenceComponentCondition, MatchingComponentCondition {
		boolean matches(MatchContext context);
	}

	private record PresenceComponentCondition(DataComponentType<?> componentType) implements ComponentCondition {
		@Override
		public boolean matches(MatchContext context) {
			return context.stack().has(this.componentType);
		}
	}

	private record MatchingComponentCondition(
		DataComponentType<?> componentType,
		Tag pattern
	) implements ComponentCondition {
		@Override
		public boolean matches(MatchContext context) {
			return context.matches(this);
		}
	}

	private static final class MatchContext {
		private final ItemStack stack;
		private final CachedResourceMatches cachedMatches;
		@Nullable
		private DynamicOps<Tag> serializationContext;

		private MatchContext(ItemStack stack, CachedResourceMatches cachedMatches) {
			this.stack = stack;
			this.cachedMatches = cachedMatches;
		}

		private ItemStack stack() {
			return this.stack;
		}

		private boolean matches(MatchingComponentCondition condition) {
			return this.cachedMatches.conditionMatches().computeIfAbsent(
				condition,
				key -> this.componentTag(key.componentType())
					.map(value -> NbtUtils.compareNbt(key.pattern(), value, true))
					.orElse(false)
			);
		}

		private Optional<Tag> componentTag(DataComponentType<?> componentType) {
			return this.cachedMatches.componentTags().computeIfAbsent(componentType, type -> {
				Object value = this.stack.get(type);
				if (value == null) {
					return Optional.empty();
				}

				try {
					return Optional.of(encodeComponent(type, value, this.serializationContext()));
				} catch (Exception e) {
					LOGGER.error("Failed to encode item component {} for adaptive trigger matching", type, e);
					return Optional.empty();
				}
			});
		}

		private DynamicOps<Tag> serializationContext() {
			if (this.serializationContext == null) {
				this.serializationContext = registryAccess().createSerializationContext(NbtOps.INSTANCE);
			}
			return this.serializationContext;
		}

		@SuppressWarnings("unchecked")
		private static Tag encodeComponent(
			DataComponentType<?> componentType,
			Object value,
			DynamicOps<Tag> serializationContext
		) {
			return ((Codec<Object>) componentType.codecOrThrow())
				.encodeStart(serializationContext, value)
				.getOrThrow();
		}
	}

	private record CachedResourceMatches(
		ItemStack snapshot,
		Map<DataComponentType<?>, Optional<Tag>> componentTags,
		Map<MatchingComponentCondition, Boolean> conditionMatches
	) {
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
