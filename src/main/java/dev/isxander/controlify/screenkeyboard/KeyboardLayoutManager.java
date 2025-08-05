package dev.isxander.controlify.screenkeyboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import net.minecraft.Util;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class KeyboardLayoutManager implements SimpleControlifyReloadListener<KeyboardLayoutManager.Preparations> {

    private static final String PREFIX = "keyboard_layout";
    private static final FileToIdConverter fileToIdConverter = FileToIdConverter.json(PREFIX);
    private static final ControlifyLogger LOGGER = CUtil.LOGGER.createSubLogger("KeyboardLayoutManager");

    private Map<ResourceLocation, KeyboardLayout> layouts = Map.of();

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> fileToIdConverter.listMatchingResources(manager),
                executor
        ).thenCompose(layoutMap -> {
            var futures = layoutMap.entrySet().stream()
                    .map(entry -> loadLayout(entry.getKey(), entry.getValue(), executor))
                    .toList();

            var map = Util.sequence(futures)
                    .thenApply(listOfEntries -> listOfEntries.stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a)));

            return map.thenApply(Preparations::new);
        });
    }

    private CompletableFuture<Map.Entry<ResourceLocation, KeyboardLayout>> loadLayout(
            ResourceLocation file, Resource resource, Executor executor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            ResourceLocation id = fileToIdConverter.fileToId(file);

            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                KeyboardLayout layout = KeyboardLayout.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(reason -> new RuntimeException("Failed to parse keyboard layout " + id + ": " + reason));

                return Map.entry(id, layout);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read keyboard layout " + id + ": " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.layouts = data.layouts();
            LOGGER.log("Loaded {} keyboard layouts", layouts.size());
        }, executor);
    }

    public Map<ResourceLocation, KeyboardLayout> getLayouts() {
        return Collections.unmodifiableMap(layouts);
    }

    public KeyboardLayout getLayout(ResourceLocation id) {
        return Objects.requireNonNull(layouts.get(id));
    }

    @Override
    public ResourceLocation getReloadId() {
        return CUtil.rl("keyboard_layout");
    }

    public record Preparations(Map<ResourceLocation, KeyboardLayout> layouts) {}
}
