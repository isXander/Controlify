package dev.isxander.controlify.controller.serialization;

import com.google.gson.JsonObject;

public interface CustomSaveLoadConfig {
    void fromJson(JsonObject json);

    void toJson(JsonObject json);
}
