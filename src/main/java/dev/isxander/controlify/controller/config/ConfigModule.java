package dev.isxander.controlify.controller.config;

import com.google.gson.JsonObject;
import dev.isxander.controlify.config.ControlifyConfig;
import net.minecraft.resources.ResourceLocation;

public record ConfigModule<T extends ConfigObject>(ResourceLocation id, Class<T> configClass) {

    public JsonObject serialize(T config) {
        return ControlifyConfig.GSON.toJsonTree(config, configClass).getAsJsonObject();
    }

    public T deserialize(JsonObject json) {
        return ControlifyConfig.GSON.fromJson(json, configClass);
    }
}
