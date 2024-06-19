package dev.isxander.controlify.controller.config;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FileDefaultSource implements DefaultSource {

    private final Supplier<JsonObject> jsonSupplier;

    private Map<ResourceLocation, JsonObject> defaults;

    public FileDefaultSource(Supplier<JsonObject> jsonSupplier) {
        this.jsonSupplier = jsonSupplier;
    }

    @Override
    public JsonObject createDefaultConfig(ResourceLocation id) {
        if (defaults == null) {
            loadDefaults();
        }

        return defaults.getOrDefault(id, new JsonObject());
    }

    public void invalidateCache() {
        defaults = null;
    }

    private void loadDefaults() {
        JsonObject json = jsonSupplier.get();
        defaults = json.entrySet().stream().collect(Collectors.toMap(
            entry -> new ResourceLocation(entry.getKey()),
            entry -> entry.getValue().getAsJsonObject()
        ));
    }
}
