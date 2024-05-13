package dev.isxander.controlify.bindings.v2.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.ControllerType;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultBindControllerReloader implements SimpleResourceReloadListener<DefaultBindControllerReloader.Preparations> {

    public static final String DIRECTORY = "controllers/default_bind";

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, NonNullDefaultBindProvider> defaultsByNamespace = new HashMap<>();

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

            Map<ResourceLocation, List<Resource>> defaultFiles = manager.listResourceStacks(DIRECTORY, id -> true);

            Map<ResourceLocation, NonNullDefaultBindProvider> defaultsByNamespace = new HashMap<>();
            for (Map.Entry<ResourceLocation, List<Resource>> stack : defaultFiles.entrySet()) {
                ResourceLocation id = stack.getKey();
                List<Resource> files = stack.getValue();

                List<LayeredDefaultBindProvider.Layer> defaults = new ArrayList<>();

                for (Resource resource : files) {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        ControllerDefault def = ControllerDefault.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

                        defaults.add(new LayeredDefaultBindProvider.Layer(def.provider(), def.clearBelow()));
                    } catch (IOException | IllegalStateException e) {
                        LOGGER.error("Failed to parse {}", id, e);
                    }
                }

                LayeredDefaultBindProvider defaultBindProvider = new LayeredDefaultBindProvider(defaults);

                ResourceLocation namespace = converter.fileToId(id);
                defaultsByNamespace.put(namespace, new NonNullDefaultBindProvider(defaultBindProvider));
            }

            return new Preparations(defaultsByNamespace);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.defaultsByNamespace.clear();
            this.defaultsByNamespace.putAll(data.map());
            if (!this.defaultsByNamespace.containsKey(ControllerType.DEFAULT.namespace())) {
                throw new IllegalStateException("Could not find default bindings to fall back on!");
            }
            System.out.println(defaultsByNamespace);
        }, executor);
    }

    public NonNullDefaultBindProvider getDefaultBindProvider(ResourceLocation namespace) {
        NonNullDefaultBindProvider provider = this.defaultsByNamespace.get(namespace);
        if (provider == null)
            provider = this.defaultsByNamespace.get(ControllerType.DEFAULT.namespace());
        return provider;
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("controlify", "default_binds");
    }

    private record ControllerDefault(boolean clearBelow, MapBackedDefaultBindProvider provider) {
        public static final Codec<ControllerDefault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("clear_below", false).forGetter(ControllerDefault::clearBelow),
                MapBackedDefaultBindProvider.CODEC.fieldOf("defaults").forGetter(ControllerDefault::provider)
        ).apply(instance, ControllerDefault::new));
    }

    public record Preparations(Map<ResourceLocation, NonNullDefaultBindProvider> map) {
    }
}
