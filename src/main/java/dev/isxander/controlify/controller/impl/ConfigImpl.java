package dev.isxander.controlify.controller.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.serialization.CustomSaveLoadConfig;
import dev.isxander.controlify.controller.serialization.IConfig;
import org.apache.commons.lang3.SerializationException;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ConfigImpl<T extends ConfigClass> implements IConfig<T> {
    private final Supplier<T> defaultFactory;
    private final Class<T> classOfT;

    private final T defaultInstance;
    private T instance;

    private @Nullable CustomSaveLoadConfig customSaveLoadConfig;

    public ConfigImpl(Supplier<T> defaultFactory, Class<T> configClass, @Nullable CustomSaveLoadConfig customSaveLoadConfig) {
        this.defaultFactory = defaultFactory;
        this.classOfT = configClass;
        this.defaultInstance = this.defaultFactory.get();
        this.instance = this.defaultFactory.get();
        this.customSaveLoadConfig = customSaveLoadConfig;
    }
    public ConfigImpl(Supplier<T> defaultFactory, Class<T> configClass) {
        this(defaultFactory, configClass, null);
    }

    @Override
    public T config() {
        return this.instance;
    }

    @Override
    public T defaultConfig() {
        return this.defaultInstance;
    }

    @Override
    public JsonElement serialize(Gson gson, ControllerEntity controller) throws SerializationException {
        try {
            this.config().onConfigSaveLoad(controller);
            JsonObject json = gson.toJsonTree(this.config(), this.classOfT).getAsJsonObject();

            if (customSaveLoadConfig != null) customSaveLoadConfig.toJson(json);

            return json;
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize config type " + this.classOfT.getTypeName(), e);
        }
    }

    @Override
    public void deserialize(JsonElement element, Gson gson, ControllerEntity controller) throws SerializationException {
        try {
            this.instance = gson.fromJson(element, this.classOfT);
            if (this.instance == null) {
                throw new IllegalStateException("Deserialized config returned null.");
            }

            if (customSaveLoadConfig != null) customSaveLoadConfig.fromJson(element.getAsJsonObject());
        } catch (Throwable e) {
            this.instance = this.defaultFactory.get();
            throw new SerializationException("Failed to deserialize type " + this.classOfT.getTypeName() + ". Resetting to default.", e);
        }
        this.instance.onConfigSaveLoad(controller);
    }

    @Override
    public void resetToDefault() {
        this.instance = this.defaultFactory.get();
    }
}
