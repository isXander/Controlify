package dev.isxander.controlify.screenkeyboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class KeyboardLayoutManager implements SimpleControlifyReloadListener<KeyboardLayoutManager.Preparations> {

    private static final String PREFIX = "keyboard_layout";
    private static final ControlifyLogger LOGGER = CUtil.LOGGER.createSubLogger("KeyboardLayoutManager");

    private Map<KeyboardLayoutKey, KeyboardLayout> layouts = Map.of();

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> listMatchingResources(manager),
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

    private CompletableFuture<Map.Entry<KeyboardLayoutKey, KeyboardLayout>> loadLayout(
            ResourceLocation file, Resource resource, Executor executor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            KeyboardLayoutKey key = fileToKey(file);

            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                KeyboardLayout layout = KeyboardLayout.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(reason -> new RuntimeException("Failed to parse keyboard layout " + key + ": " + reason));

                return Map.entry(key, layout);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read keyboard layout " + key + ": " + e.getMessage(), e);
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

    public KeyboardLayoutWithId getLayout(ResourceLocation layoutId, String languageCode) {
        var key = new KeyboardLayoutKey(languageCode, layoutId);

        return Optional.ofNullable(this.layouts.get(key))
                .or(() -> Optional.ofNullable(this.layouts.get(key.withDefaultLanguage())))
                .map(layout -> new KeyboardLayoutWithId(layout, layoutId))
                .orElse(KeyboardLayouts.fallback());
    }

    public KeyboardLayoutWithId getLayout(ResourceLocation layout) {
        String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected();
        return getLayout(layout, currentLanguage);
    }

    @Override
    public ResourceLocation getReloadId() {
        return CUtil.rl("keyboard_layout");
    }

    private static ResourceLocation keyToFile(KeyboardLayoutKey key) {
        return key.layoutId().withPath(PREFIX + "/" + key.layoutId().getPath() + "/" + key.languageCode() + ".json");
    }

    private static KeyboardLayoutKey fileToKey(ResourceLocation file) {
        try {
            var components = file.getPath().split("/");
            var layoutPath = components[1];
            var languageCodeWithExt = components[2];
            var languageCode = languageCodeWithExt.substring(0, languageCodeWithExt.lastIndexOf('.'));
            var layoutId = file.withPath(layoutPath);

            return new KeyboardLayoutKey(languageCode, layoutId);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid file path. Expected format: keyboard_layout/<layout_id>/<language_code>.json, but got " + file.getPath(), e);
        }

    }

    private static Map<ResourceLocation, Resource> listMatchingResources(ResourceManager resourceManager) {
        return resourceManager.listResources(PREFIX, path -> path.getPath().endsWith(".json"));
    }

    public record Preparations(Map<KeyboardLayoutKey, KeyboardLayout> layouts) {}

    private record KeyboardLayoutKey(String languageCode, ResourceLocation layoutId) {
        public static final String DEFAULT_LANGUAGE = "en_us";

        public KeyboardLayoutKey withLanguage(String languageCode) {
            return new KeyboardLayoutKey(languageCode, this.layoutId);
        }

        public KeyboardLayoutKey withDefaultLanguage() {
            return new KeyboardLayoutKey(DEFAULT_LANGUAGE, this.layoutId);
        }
    }
}
