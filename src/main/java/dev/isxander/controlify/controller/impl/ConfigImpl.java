package dev.isxander.controlify.controller.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.controller.IConfig;
import org.apache.commons.lang3.SerializationException;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public class ConfigImpl<T> implements IConfig<T> {
    private final Supplier<T> defaultFactory;
    private final Class<T> classOfT;

    private final T defaultInstance;
    private T instance;

    public ConfigImpl(Supplier<T> defaultFactory, Class<T> configClass) {
        this.defaultFactory = defaultFactory;
        this.classOfT = configClass;
        this.defaultInstance = this.defaultFactory.get();
        this.instance = this.defaultFactory.get();
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
    public JsonElement serialize(Gson gson) throws SerializationException {
        try {
            return gson.toJsonTree(this.config(), this.classOfT);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize config type " + this.classOfT.getTypeName(), e);
        }
    }

    @Override
    public void deserialize(JsonElement element, Gson gson) throws SerializationException {
        try {
            this.instance = gson.fromJson(element, this.classOfT);
        } catch (Exception e) {
            this.instance = this.defaultFactory.get();
            throw new SerializationException("Failed to deserialize type " + this.classOfT.getTypeName() + ". Resetting to default.", e);
        }
    }

    @Override
    public void resetToDefault() {
        this.instance = this.defaultFactory.get();
    }
}
