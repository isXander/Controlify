package dev.isxander.controlify.bindings.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultBindManager implements SimpleControlifyReloadListener<DefaultBindManager.Preparations> {

    public static final String DIRECTORY = "controllers/default_bind";
    private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, DefaultBindProvider> defaultsByNamespace = new HashMap<>();

    @Override
    public CompletableFuture<@Nullable Preparations> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, List<Resource>> defaultFiles = converter.listMatchingResourceStacks(manager);

            Map<ResourceLocation, DefaultBindProvider> defaultsByNamespace = new HashMap<>();

            ResourceLocation defaultNamespaceFile = converter.idToFile(ControllerType.DEFAULT.namespace());
            if (!defaultFiles.containsKey(defaultNamespaceFile)) {
                LOGGER.error("No default binds found! Everything will be unbound!");
                return null;
            }
            LayeredDefaultBindProvider defaultNamespaceDefaults = new LayeredDefaultBindProvider(
                    this.readDefaults(
                            defaultNamespaceFile,
                            defaultFiles.get(defaultNamespaceFile)
                    ).getSecond()
            ); // default namespace for the defaults!
            defaultsByNamespace.put(ControllerType.DEFAULT.namespace(), defaultNamespaceDefaults);

            for (Map.Entry<ResourceLocation, List<Resource>> stack : defaultFiles.entrySet()) {
                ResourceLocation id = stack.getKey();
                List<Resource> files = stack.getValue();

                if (id.equals(defaultNamespaceFile))
                    continue; // already processed

                Pair<ResourceLocation, List<LayeredDefaultBindProvider.Layer>> defaults = this.readDefaults(id, files);
                // add the default namespace to the bottom
                defaults.getSecond().add(
                        new LayeredDefaultBindProvider.Layer(defaultNamespaceDefaults, false)
                );

                LayeredDefaultBindProvider defaultBindProvider = new LayeredDefaultBindProvider(defaults.getSecond());
                defaultsByNamespace.put(defaults.getFirst(), defaultBindProvider);
            }

            return new Preparations(defaultsByNamespace);
        }, executor);
    }

    private Pair<ResourceLocation, List<LayeredDefaultBindProvider.Layer>> readDefaults(ResourceLocation id, List<Resource> files) {
        List<LayeredDefaultBindProvider.Layer> defaults = new ArrayList<>();

        for (Resource resource : files) {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                ControllerDefault def = ControllerDefault.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

                // add to top takes priority
                defaults.add(0, new LayeredDefaultBindProvider.Layer(def.provider(), def.clearBelow()));
            } catch (IOException | IllegalStateException e) {
                LOGGER.error("Failed to parse {}", id, e);
            }
        }

        ResourceLocation namespace = converter.fileToId(id);

        return Pair.of(namespace, defaults);
    }

    @Override
    public CompletableFuture<Void> apply(@Nullable Preparations data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            List<InputBinding> defaultedBindings = new ArrayList<>();
            for (ControllerEntity controller : Controlify.instance().getControllerManager().map(ControllerManager::getConnectedControllers).orElse(List.of())) {
                controller.input().ifPresent(input -> {
                    if (!input.confObj().keepDefaultBindings) {
                        for (InputBinding binding : input.getAllBindings()) {
                            if (binding.boundInput().equals(binding.defaultInput())) {
                                defaultedBindings.add(binding);
                            }
                        }
                    }
                });
            }

            this.defaultsByNamespace.clear();
            if (data != null) {
                this.defaultsByNamespace.putAll(data.map());
            }

            for (InputBinding binding : defaultedBindings) {
                binding.setBoundInput(binding.defaultInput());
            }
        }, executor);
    }

    public DefaultBindProvider getDefaultBindProvider(ResourceLocation namespace) {
        DefaultBindProvider provider = this.defaultsByNamespace.get(namespace);
        if (provider == null)
            provider = this.defaultsByNamespace.get(ControllerType.DEFAULT.namespace());
        if (provider == null)
            provider = DefaultBindProvider.EMPTY;
        return provider;
    }

    @Override
    public ResourceLocation getReloadId() {
        return CUtil.rl("default_binds");
    }

    private record ControllerDefault(boolean clearBelow, MapBackedDefaultBindProvider provider) {
        public static final Codec<ControllerDefault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("clear_below", false).forGetter(ControllerDefault::clearBelow),
                MapBackedDefaultBindProvider.MAP_CODEC.fieldOf("defaults").forGetter(ControllerDefault::provider)
        ).apply(instance, ControllerDefault::new));
    }

    public record Preparations(Map<ResourceLocation, DefaultBindProvider> map) {
    }
}
