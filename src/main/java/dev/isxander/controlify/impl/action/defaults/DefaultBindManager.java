/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultBindManager implements SimpleControlifyReloadListener<DefaultBindManager.Preparations> {

	public static final String DIRECTORY = "controller/default_bind";
	private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<Identifier, DefaultBindProvider> defaultsByNamespace = new HashMap<>();

	@Override
	public CompletableFuture<@Nullable Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, List<Resource>> defaultFiles = converter.listMatchingResourceStacks(manager);

			Map<Identifier, DefaultBindProvider> defaultsByNamespace = new HashMap<>();

			Identifier defaultNamespaceFile = converter.idToFile(ControllerType.DEFAULT.id());
			if (!defaultFiles.containsKey(defaultNamespaceFile)) {
				LOGGER.error("No default binds found! Everything will be unbound!");
				return null;
			}
			LayeredDefaultBindProvider defaultNamespaceDefaults = new LayeredDefaultBindProvider(
					this.readDefaults(
							defaultNamespaceFile,
							defaultFiles.get(defaultNamespaceFile)
					).getSecond()
			); // default id for the defaults!
			defaultsByNamespace.put(ControllerType.DEFAULT.id(), defaultNamespaceDefaults);

			for (Map.Entry<Identifier, List<Resource>> stack : defaultFiles.entrySet()) {
				Identifier id = stack.getKey();
				List<Resource> files = stack.getValue();

				if (id.equals(defaultNamespaceFile)) {
					continue; // already processed
				}

				Pair<Identifier, List<LayeredDefaultBindProvider.Layer>> defaults = this.readDefaults(id, files);
				// add the default id to the bottom
				defaults.getSecond().add(
						new LayeredDefaultBindProvider.Layer(defaultNamespaceDefaults, false)
				);

				LayeredDefaultBindProvider defaultBindProvider = new LayeredDefaultBindProvider(defaults.getSecond());
				defaultsByNamespace.put(defaults.getFirst(), defaultBindProvider);
			}

			return new Preparations(defaultsByNamespace);
		}, executor);
	}

	private Pair<Identifier, List<LayeredDefaultBindProvider.Layer>> readDefaults(Identifier id, List<Resource> files) {
		List<LayeredDefaultBindProvider.Layer> defaults = new ArrayList<>();

		for (Resource resource : files) {
			try (BufferedReader reader = resource.openAsReader()) {
				JsonElement json = JsonParser.parseReader(reader);
				DataResult<ControllerDefault> data = ControllerDefault.CODEC.parse(JsonOps.INSTANCE, json);

				var error = data.error();
				if (error.isPresent()) {
					LOGGER.error("Failed to parse for id {}: {}", id, error.get().message());
					continue;
				}

				ControllerDefault def = data.getOrThrow();

				// add to top takes priority
				defaults.addFirst(new LayeredDefaultBindProvider.Layer(def.provider(), def.clearBelow()));
			} catch (IOException | IllegalStateException e) {
				LOGGER.error("Failed to parse {}", id, e);
			}
		}

		Identifier namespace = converter.fileToId(id);

		return Pair.of(namespace, defaults);
	}

	@Override
	public CompletableFuture<Void> apply(@Nullable Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			// Update the default providers
			this.defaultsByNamespace.clear();
			if (data != null) {
				this.defaultsByNamespace.putAll(data.map());
			}
			// Refresh action defaults from the new providers without rewriting saved bindings.
			Controlify.instance().getControllerManager().ifPresent(controllerManager -> {
				for (var controller : controllerManager.getAllControllers()) {
					controller.input().reloadDefaultInputs();
				}
			});
		}, executor);
	}

	public DefaultBindProvider getDefaultBindProvider(Identifier namespace) {
		DefaultBindProvider provider = this.defaultsByNamespace.get(namespace);
		if (provider == null) {
			provider = this.defaultsByNamespace.get(ControllerType.DEFAULT.id());
		}
		if (provider == null) {
			provider = DefaultBindProvider.EMPTY;
		}
		return provider;
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("default_binds");
	}

	private record ControllerDefault(boolean clearBelow, MapBackedDefaultBindProvider provider) {
		public static final Codec<ControllerDefault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("clear_below", false).forGetter(ControllerDefault::clearBelow),
				MapBackedDefaultBindProvider.MAP_CODEC.fieldOf("defaults").forGetter(ControllerDefault::provider)
		).apply(instance, ControllerDefault::new));
	}

	public record Preparations(Map<Identifier, DefaultBindProvider> map) {
	}
}
