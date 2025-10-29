package dev.isxander.controlify.input.action.gesture;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.action.gesture.builder.GestureDecoderHelper;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.Util;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages the loading of default gesture bindings for actions.
 * <p>
 * Each controller namespace can have its own set of default bindings.
 * The default namespace <code>controlify:default</code> is used as a base layer.
 * Each resource-pack, and then each namespace, can {@link dev.isxander.controlify.input.action.gesture.builder.GestureBuilder#merge(GestureBuilder)} with the previous layer to build a layer.
 */
public class DefaultGestureManager implements SimpleControlifyReloadListener<DefaultGestureManager.Preparations> {

    public static final String DIRECTORY = "controllers/default_bind";
    private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<ResourceLocation, Map<ResourceLocation, Gesture>> gesturesByNamespaceByAction = Map.of();

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
        // get a list of resource stacks for each namespace
        // controlify:namespace -> [main pack, resource pack 1, resource pack 2, ...]
        Map<ResourceLocation, List<Resource>> defaultFiles = converter.listMatchingResourceStacks(manager);

        ResourceLocation defaultNamespace = ControllerType.DEFAULT.namespace();
        if (!defaultFiles.containsKey(defaultNamespace)) {
            LOGGER.error("No default binds found! Everything will be unbound!");
            return null;
        }

        CompletableFuture<Map<ResourceLocation, GestureDecoderHelper>> defaultNamespaceFuture =
                CompletableFuture.supplyAsync(
                        () -> this.processStack(defaultFiles.remove(defaultNamespace), null),
                        executor
                );

        // parse all non-default namespaces in parallel
        // each namespace can inherit from the default namespace
        List<CompletableFuture<Map.Entry<ResourceLocation, Map<ResourceLocation, Gesture>>>> namespacedFutures = defaultFiles.entrySet().stream()
                .map(e -> defaultNamespaceFuture.thenApplyAsync(defaultNamespaceGesturesByAction -> {
                    ResourceLocation namespace = e.getKey();
                    // process each stack, using the default namespace as a base for gestures
                    Map<ResourceLocation, Gesture> gesturesByAction = this.finish(this.processStack(e.getValue(), defaultNamespaceGesturesByAction::get));

                    return Map.entry(namespace, gesturesByAction);
                }, executor))
                .toList();

        // when all namespaces are done, combine them into a single map and future
        CompletableFuture<Map<ResourceLocation, Map<ResourceLocation, Gesture>>> allNamespacesFuture = Util.sequence(namespacedFutures)
                .thenApply(el -> el.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)))
                .thenCombine(defaultNamespaceFuture, (m, def) -> {
                    m.put(defaultNamespace, this.finish(def));
                    return m;
                });

        return allNamespacesFuture.thenApply(Preparations::new);
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.gesturesByNamespaceByAction = data.gesturesByNamespaceByAction();
        }, executor);
    }

    private Map<ResourceLocation, GestureDecoderHelper> processStack(List<Resource> stack, @Nullable Function<ResourceLocation, GestureDecoderHelper> baseGetter) {
        Map<ResourceLocation, GestureDecoderHelper> gesturesByAction = new HashMap<>();

        for (Resource resource : stack) {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                Map<String, JsonElement> rawMap = json.getAsJsonObject().getAsJsonObject("defaults").asMap();
                for (Map.Entry<String, JsonElement> entry : rawMap.entrySet()) {
                    ResourceLocation actionId = ResourceLocation.parse(entry.getKey());
                    GestureDecoderHelper decoderHelper = gesturesByAction.computeIfAbsent(actionId, k -> new GestureDecoderHelper(baseGetter != null ? baseGetter.apply(k) : null));
                    decoderHelper.push(new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load default binds", e);
            }
        }

        return gesturesByAction;
    }

    private Map<ResourceLocation, Gesture> finish(Map<ResourceLocation, GestureDecoderHelper> helpersByAction) {
        return helpersByAction.entrySet().stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().build()), HashMap::putAll);
    }

    @Override
    public ResourceLocation getReloadId() {
        return CUtil.rl("default_bind");
    }

    public Optional<Gesture> getGesture(ResourceLocation namespace, ResourceLocation action) {
        return Optional.ofNullable(this.gesturesByNamespaceByAction
                .getOrDefault(namespace, this.getDefaultNamespaceGestures())
                .get(action));
    }

    private Map<ResourceLocation, Gesture> getDefaultNamespaceGestures() {
        return this.gesturesByNamespaceByAction.get(ControllerType.DEFAULT.namespace());
    }


    public record Preparations(Map<ResourceLocation, Map<ResourceLocation, Gesture>> gesturesByNamespaceByAction) {}
}
