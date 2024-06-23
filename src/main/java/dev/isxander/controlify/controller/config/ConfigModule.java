package dev.isxander.controlify.controller.config;

import com.google.gson.JsonObject;
import dev.isxander.controlify.config.ControlifyConfig;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public record ConfigModule<T extends ConfigObject>(ResourceLocation id, Class<T> configClass) {

    public JsonObject serialize(T config, T defaultConfig) {
        JsonObject configJson = ControlifyConfig.GSON.toJsonTree(config, configClass).getAsJsonObject();
        JsonObject defaultJson = ControlifyConfig.GSON.toJsonTree(defaultConfig, configClass).getAsJsonObject();

        // deep compare and remove default values
        CUtil.removeMatchingKeysDeep(configJson, defaultJson);

        return configJson;
    }

    public T deserialize(JsonObject json) {
        return ControlifyConfig.GSON.fromJson(json, configClass);
    }
}
