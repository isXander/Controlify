package dev.isxander.controlify.input.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;

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
			Map<Identifier, ControllerMapping> mappingById = resources.entrySet().stream()
					.map(entry -> Map.entry(entry.getKey(), parseFile(entry.getValue())))
					.collect(Util.toMap());

			return new Preparations(mappingById);
		}, executor);
	}

	private static ControllerMapping parseFile(Resource resource) {
		try (BufferedReader reader = resource.openAsReader()) {
			JsonElement json = JsonParser.parseReader(reader);
			DataResult<ControllerMapping> mappingResult = ControllerMapping.CODEC.parse(JsonOps.INSTANCE, json);
			return mappingResult.getOrThrow();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.mappingById.clear();
			this.mappingById.putAll(data.mappingById);
		}, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("controller_mapping");
	}

	public record Preparations(Map<Identifier, ControllerMapping> mappingById) {
	}
}
