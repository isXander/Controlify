package dev.isxander.controlify.config.dto.profile.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultConfigManager implements SimpleControlifyReloadListener<DefaultConfigManager.Preparations>, DefaultConfigProvider {

    public static final String DIRECTORY = "controllers/default_config";
    private static final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<Identifier, ProfileConfig> defaultsByNamespace = new HashMap<>();

    @Override
    public ProfileConfig getDefaultForNamespace(@Nullable Identifier namespace) {
        if (namespace == null) {
            namespace = ControllerType.DEFAULT.namespace();
        }
        return defaultsByNamespace.getOrDefault(namespace, defaultsByNamespace.get(ControllerType.DEFAULT.namespace()));
    }

    @Override
    public boolean isReady() {
        return !defaultsByNamespace.isEmpty();
    }

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, List<Resource>> defaultFiles = converter.listMatchingResourceStacks(manager);

            Map<Identifier, ProfileConfig> defaultsByNamespace = new HashMap<>();

            Identifier defaultNamespaceFile = converter.idToFile(ControllerType.DEFAULT.namespace());
            if (!defaultFiles.containsKey(defaultNamespaceFile)) {
                var report = CrashReport.forThrowable(new Throwable(), "No default Controlify configs found! Cannot continue.");
                throw new ReportedException(report);
            }

            JsonObject defaultNamespaceJson = this.readDefaults(
                    new JsonObject(),
                    defaultNamespaceFile,
                    defaultFiles.get(defaultNamespaceFile)
            );
            defaultsByNamespace.put(ControllerType.DEFAULT.namespace(), jsonToConfig(defaultNamespaceJson));

            for (Map.Entry<Identifier, List<Resource>> stack : defaultFiles.entrySet()) {
                Identifier fileId = stack.getKey();
                if (fileId.equals(defaultNamespaceFile)) continue;

                JsonObject combinedJson = this.readDefaults(
                        defaultNamespaceJson,
                        fileId,
                        stack.getValue()
                );
                defaultsByNamespace.put(converter.fileToId(fileId), jsonToConfig(combinedJson));
            }

            return new Preparations(defaultsByNamespace);
        });
    }

    private JsonObject readDefaults(JsonObject base, Identifier resourceId, List<Resource> resources) {
        JsonObject combined = base.deepCopy();
        for (Resource resource : resources) {
            try (var reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader).getAsJsonObject();
                // merge deeply
                mergeJsonObjects(combined, json.getAsJsonObject());
            } catch (Exception e) {
                LOGGER.error("Failed to read default config file: {}", resourceId, e);
            }
        }

        return combined;
    }

    private ProfileConfig jsonToConfig(JsonObject json) {
        DataResult<ProfileConfig> result = ProfileConfig.CODEC.parse(JsonOps.INSTANCE, json);
        return result.result().orElseThrow(() -> new IllegalStateException("Failed to parse ControlifyConfig from JSON: " + result.error().orElse(null)));
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.defaultsByNamespace.clear();
            if (data != null) {
                this.defaultsByNamespace.putAll(data.map);
            }
        });
    }

    @Override
    public Identifier getReloadId() {
        return CUtil.rl("default_config");
    }

    public record Preparations(Map<Identifier, ProfileConfig> map) {}

    public static void mergeJsonObjects(JsonObject target, JsonObject source) {
        for (String key : source.keySet()) {
            JsonElement sourceElement = source.get(key);
            if (target.has(key)) {
                JsonElement targetElement = target.get(key);
                if (sourceElement.isJsonObject() && targetElement.isJsonObject()) {
                    mergeJsonObjects(targetElement.getAsJsonObject(), sourceElement.getAsJsonObject());
                } else {
                    target.add(key, sourceElement);
                }
            } else {
                target.add(key, sourceElement);
            }
        }
    }
}
