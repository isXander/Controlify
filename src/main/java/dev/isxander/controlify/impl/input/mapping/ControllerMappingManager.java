/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ControllerMappingManager implements SimpleControlifyReloadListener<ControllerMappingManager.Preparations> {

	private static final FileToIdConverter converter = FileToIdConverter.json("controller/mapping");

	private final Map<Identifier, ControllerMapping> mappingById = new HashMap<>();

	public Optional<ControllerMapping> getMappingById(Identifier id) {
		return Optional.ofNullable(this.mappingById.get(id));
	}

	@Override
	public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, Resource> resources = converter.listMatchingResources(manager);
			Map<Identifier, ControllerMapping> mappingById = new HashMap<>();
			resources.forEach((fileId, resource) -> {
				Identifier id = converter.fileToId(fileId);
				// A malformed mapping must not stop the others, or the rest of the reload, from loading.
				parseFile(resource).ifPresentOrElse(
						mapping -> mappingById.put(id, mapping),
						() -> CUtil.LOGGER.error("Skipping invalid controller mapping {} (from {})", id, fileId));
			});

			return new Preparations(mappingById);
		}, executor);
	}

	private static Optional<ControllerMapping> parseFile(Resource resource) {
		try (BufferedReader reader = resource.openAsReader()) {
			JsonElement json = JsonParser.parseReader(reader);
			DataResult<ControllerMapping> mappingResult = ControllerMapping.CODEC.parse(JsonOps.INSTANCE, json);
			mappingResult.error().ifPresent(error -> CUtil.LOGGER.error("Invalid controller mapping: {}", error.message()));
			return mappingResult.result();
		} catch (IOException | RuntimeException e) {
			CUtil.LOGGER.error("Failed to read controller mapping", e);
			return Optional.empty();
		}
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.mappingById.clear();
			this.mappingById.putAll(data.mappingById);
			Controlify.instance().getControllerManager().ifPresent(controllerManager -> {
				for (var controller : controllerManager.getAllControllers()) {
					controller.input().reloadControllerMapping();
				}
			});
		}, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("controller_mapping");
	}

	public record Preparations(Map<Identifier, ControllerMapping> mappingById) {
	}
}
