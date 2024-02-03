package dev.isxander.controlify.controller.composable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.controller.ControllerConfig;

public interface ComposableControllerConfig<T extends ControllerConfig> {
    T config();

    T defaultConfig();

    void resetConfig();

    void setConfig(Gson gson, JsonElement json, ComposableController<?> controller);

    default void close() {}
}
