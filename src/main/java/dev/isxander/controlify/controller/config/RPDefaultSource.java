package dev.isxander.controlify.controller.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.Util;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RPDefaultSource implements SimpleControlifyReloadListener<List<RPDefaultSource.DefaultEntry>>, DefaultSource {
    private final ModuleRegistry registry;
    private final Map<ResourceLocation, JsonObject> configs;

    private static final FileToIdConverter fileToId = FileToIdConverter.json("controllers/default_config");

    public RPDefaultSource(ModuleRegistry registry) {
        this.registry = registry;
        this.configs = new HashMap<>();
    }

    @Override
    public CompletableFuture<List<DefaultEntry>> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        var futures = registry.getModules().stream()
                .map(info -> CompletableFuture.supplyAsync(() -> loadModule(info, manager), executor))
                .toList();
        return Util.sequence(futures)
                .thenApply(list -> list.stream().flatMap(Optional::stream).toList());
    }

    @Override
    public CompletableFuture<Void> apply(List<DefaultEntry> data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            configs.clear();
            data.forEach(entry -> configs.put(entry.id(), entry.config()));
        }, executor);
    }

    private <T extends ConfigObject> Optional<DefaultEntry> loadModule(ConfigModule<T> info, ResourceManager resourceManager) {
        ResourceLocation fileId = fileToId.idToFile(info.id());
        Optional<Resource> resourceOpt = resourceManager.getResource(fileId);

        if (resourceOpt.isEmpty())
            return Optional.empty();

        Resource resource = resourceOpt.get();

        JsonElement json;
        try (BufferedReader reader = resource.openAsReader()) {
            json = JsonParser.parseReader(reader);
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(new DefaultEntry(info.id(), json.getAsJsonObject()));
    }

    @Override
    public JsonObject createDefaultConfig(ResourceLocation id) {
        return this.configs.getOrDefault(id, new JsonObject());
    }

    @Override
    public ResourceLocation getReloadId() {
        return CUtil.rl("default_config");
    }

    public record DefaultEntry(ResourceLocation id, JsonObject config) {
    }
}
