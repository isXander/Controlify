package dev.isxander.controlify.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.ControlifyConfig;
import dev.isxander.controlify.config.dto.dfu.ControlifyDataFixer;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.settings.ControlifySettings;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Path path;

    private ControlifySettings settings;
    private boolean dirty = false;

    public ConfigManager(Path path) {
        this.path = path;
        this.settings = ControlifySettings.defaults();
    }

    public ControlifySettings getSettings() {
        return settings;
    }

    public void loadOrDefault() {
        boolean loadResult = false;
        try {
            loadResult = load();
        } catch (IOException e) {
            LOGGER.error("Failed to load config, re-saving and using defaults.", e);
        }

        if (!loadResult) {
            this.settings = ControlifySettings.defaults();
            try {
                this.save();
            } catch (IOException e) {
                throw new RuntimeException("Failed to save default config", e);
            }
        }
    }

    public boolean load() throws IOException {
        if (Files.notExists(this.path)) {
            return false;
        }

        String jsonString = Files.readString(this.path);
        JsonElement json;
        try {
            json = JsonParser.parseString(jsonString);
        } catch (JsonParseException e) {
            throw new IOException("Failed to parse config JSON", e);
        }

        Dynamic<?> dynamic = new Dynamic<>(JsonOps.INSTANCE, json);

        int schemaVersion = dynamic.get("schema_version").asInt(-1);
        if (schemaVersion == -1) {
            throw new IOException("Config is missing schema_version field");
        }

        Dynamic<?> fixed = ControlifyDataFixer.getFixer().update(
                ControlifyTypeReferences.USER_STATE,
                dynamic,
                schemaVersion,
                ControlifyDataFixer.CURRENT_VERSION
        );

        DataResult<ControlifyConfig> dtoResult = ControlifyConfig.CODEC.parse(fixed);
        if (dtoResult.isError()) {
            throw new IOException("Failed to decode config DTO: " + dtoResult.error().map(DataResult.Error::message).orElse(""));
        }
        ControlifyConfig dto = dtoResult.result().orElseThrow(() -> new IOException("Failed to decode config DTO"));

        this.settings = ControlifySettings.fromDTO(dto);

        return true;
    }

    public boolean save() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("schema_version", ControlifyDataFixer.CURRENT_VERSION);

        JsonObject configObject = ControlifyConfig.CODEC
                .encodeStart(JsonOps.INSTANCE, this.settings.toDTO())
                .result()
                .orElseThrow(() -> new IOException("Failed to encode config DTO"))
                .getAsJsonObject();

        for (String key : configObject.keySet()) {
            jsonObject.add(key, configObject.get(key));
        }

        String jsonString = jsonObject.toString();

        Files.writeString(this.path, jsonString);
        return true;
    }

    public boolean saveSafely() {
        try {
            boolean result = this.save();
            this.dirty = false;
            return result;
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
        return false;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void saveIfDirty() {
        if (this.dirty) {
            this.saveSafely();
        }
    }
}
