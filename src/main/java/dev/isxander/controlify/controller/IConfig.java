package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.SerializationException;

public interface IConfig<T> extends ECSComponent {
    T config();

    T defaultConfig();

    JsonElement serialize(Gson gson) throws SerializationException;

    void deserialize(JsonElement element, Gson gson) throws SerializationException;

    void resetToDefault();
}
