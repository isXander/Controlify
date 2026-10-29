/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.impl.action.ActionImpl;
import dev.isxander.controlify.impl.action.activator.LeafConfig;
import net.minecraft.ChatFormatting;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputFontMapper implements SimpleControlifyReloadListener<InputFontMapper.Preparations> {
	private ImmutableMap<Identifier, FontMap> mappings = ImmutableMap.of();
	private FontMap defaultFontMap;

	private static final Codec<Character> CHAR_CODEC = Codec.STRING.comapFlatMap(
			(str) -> {
				if (str.length() != 1) {
					return DataResult.error(() -> "Expected a single character string, got " + str);
				}
				return DataResult.success(str.charAt(0));
			},
			String::valueOf
	);

	private static final Codec<Pair<Character, Map<Identifier, Character>>> FONT_MAP_CODEC = Codec.pair(
			CHAR_CODEC.fieldOf("unknown").codec(),
			Codec.unboundedMap(Identifier.CODEC, CHAR_CODEC)
	);

	private static final FileToIdConverter fileToIdConverter = FileToIdConverter.json("controller/font_mapping");

	@Override
	public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, Resource> mappingResources = fileToIdConverter.listMatchingResources(manager);
			Map<Identifier, FontMap> mappings = mappingResources.entrySet().stream().flatMap(entry -> {
				Identifier rl = entry.getKey();
				Resource resource = entry.getValue();

				Identifier namespace = fileToIdConverter.fileToId(rl);
				try (BufferedReader reader = resource.openAsReader()) {
					JsonElement element = JsonParser.parseReader(reader);
					FontMap map = FONT_MAP_CODEC.parse(JsonOps.INSTANCE, element)
							.resultOrPartial(CUtil.LOGGER::error)
							.map(pair -> new FontMap(namespace, pair.getFirst(), pair.getSecond()))
							.orElse(null);
					if (map != null) {
						return Stream.of(Pair.of(namespace, map));
					}
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load font mappings for id {}", namespace, e);
				}

				return Stream.empty();
			}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

			return new Preparations(mappings);
		});
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			ImmutableMap.Builder<Identifier, FontMap> builder = ImmutableMap.builder();
			data.mappings().forEach(builder::put);
			mappings = builder.build();
			defaultFontMap = mappings.get(ControllerType.DEFAULT.id());
		}, executor);
	}

	public FontMap getMappings(Identifier namespace) {
		return mappings.getOrDefault(namespace, defaultFontMap);
	}

	/// The action's primary binding, as shown in prompts. Its gesture is only spelled out when the action
	/// lets bindings choose one and another currently active action shares the input, since the glyph
	/// alone would not say which of them a press triggers.
	public MutableComponent getComponentFromAction(Identifier namespace, @Nullable Action action) {
		if (action == null || action.bindings().isEmpty()) {
			// TODO: implement some sort of "unbound" character
			return Component.literal("?");
		}

		ActionBinding primary = action.bindings().getFirst();
		Optional<LeafConfig> shownGesture = editableLeaf(action)
				.filter(_ -> action instanceof ActionImpl impl && impl.isInputShared(primary.input()));
		return getComponentFromBinding(namespace, primary, shownGesture);
	}

	/// The binding's input glyph followed by its gesture, such as "A (Hold)".
	/// The gesture is only shown when {@code editableLeaf} is present, since it is otherwise fixed by the action.
	public MutableComponent getComponentFromBinding(Identifier namespace, @Nullable ActionBinding binding, Optional<LeafConfig> editableLeaf) {
		if (binding == null) {
			return getComponentFromInput(namespace, null);
		}
		var input = getComponentFromInput(namespace, binding.input());
		return editableLeaf.flatMap(leaf -> leaf.gestureFor(binding))
				.map(gesture -> Component.empty()
						.append(input)
						.append(Component.literal(" ").withStyle(style -> style.withFont(FontDescription.DEFAULT)))
						.append(gestureLabel(gesture)))
				.orElse(input);
	}

	public static Optional<LeafConfig> editableLeaf(Action action) {
		return action instanceof ActionImpl impl ? impl.activatorConfig().editableLeaf() : Optional.empty();
	}

	public static MutableComponent gestureLabel(Gesture gesture) {
		return Component.translatable("controlify.gesture." + gesture.getSerializedName())
				.withStyle(style -> style.withFont(FontDescription.DEFAULT).withColor(ChatFormatting.GRAY));
	}

	public MutableComponent getComponentFromInput(Identifier namespace, @Nullable LogicalInput input) {
		if (input == null) {
			return Component.literal("<unbound>");
		}

		FontMap fontMap = getMappings(namespace);
		return switch (input) {
			case LogicalInput.Single(var physicalInput) -> {
				MappedChar mappedChar = getChar(fontMap, physicalInput);
				yield createComponent(String.valueOf(mappedChar.character()), mappedChar.fontMap());
			}
			case LogicalInput.ModifiedChord(var modifiers, var trigger) -> {
				MutableComponent component = Component.empty();
				StringBuilder literal = new StringBuilder();
				FontMap currentFontMap = null;

				// Coalesce adjacent modifiers using the same font into a single literal.
				for (Identifier modifier : modifiers) {
					MappedChar mappedChar = getChar(fontMap, modifier);
					if (currentFontMap != null && currentFontMap != mappedChar.fontMap()) {
						MutableComponent run = createComponent(literal.toString(), currentFontMap);
						component.append(run);
						literal.setLength(0);
					}
					currentFontMap = mappedChar.fontMap();
					literal.append(mappedChar.character());
				}

				if (currentFontMap != null) {
					MutableComponent run = createComponent(literal.toString(), currentFontMap);
					// Use the first run as the root to avoid an empty wrapper component.
					component.append(run);
					component.append(Component.literal("+").withStyle(style -> style.withFont(FontDescription.DEFAULT)));
				}

				MappedChar mappedTrigger = getChar(fontMap, trigger);
				MutableComponent triggerComponent = createComponent(String.valueOf(mappedTrigger.character()), mappedTrigger.fontMap());
				yield component.append(triggerComponent);
			}
		};
	}

	private MutableComponent createComponent(String literal, FontMap fontMap) {
		return Component.literal(literal).withStyle(style -> style
				.withFont(new FontDescription.Resource(fontMap.namespace().withPrefix("controller/")))
				.withShadowColor(0x00000000) // remove shadow
				.withColor(0xFFFFFFFF)); // override color of font renderer so the glyph always renders properly
	}

	private MappedChar getChar(FontMap fontMap, Identifier input) {
		Character ch = fontMap.inputToChar().get(input);
		if (ch != null) {
			return new MappedChar(ch, fontMap);
		}

		// fallback to default icon set if there is no mapping
		ch = defaultFontMap.inputToChar().get(input);
		if (ch != null) {
			return new MappedChar(ch, defaultFontMap);
		}

		return new MappedChar(fontMap.unknown(), fontMap);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("font_mappings");
	}

	public record Preparations(Map<Identifier, FontMap> mappings) {
	}
	public record FontMap(Identifier namespace, char unknown, Map<Identifier, Character> inputToChar) {
	}

	private record MappedChar(char character, FontMap fontMap) {
	}
}
