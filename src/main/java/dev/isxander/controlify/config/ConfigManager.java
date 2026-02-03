package dev.isxander.controlify.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.ControlifyConfig;
import dev.isxander.controlify.config.dto.dfu.ControlifyDataFixer;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.dto.profile.defaults.DefaultConfigManager;
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
            LOGGER.error("Failed to load config, making backup, re-saving, and using defaults.", e);
            this.makeBackup();
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

        boolean requiresSaving = false;

        String jsonString = Files.readString(this.path);
        JsonElement json;
        try {
            json = JsonParser.parseString(jsonString);
        } catch (JsonParseException e) {
            throw new IOException("Failed to parse config JSON", e);
        }

        Dynamic<?> dynamic = new Dynamic<>(JsonOps.INSTANCE, json);

        // Files without a schema_version are considered version 0 since that was before we started versioning
        int schemaVersion = dynamic.get("schema_version").asInt(0);

        Dynamic<?> fixed = ControlifyDataFixer.getFixer().update(
                ControlifyTypeReferences.USER_STATE,
                dynamic,
                schemaVersion,
                ControlifyDataFixer.CURRENT_VERSION
        );

        if (schemaVersion != ControlifyDataFixer.CURRENT_VERSION) {
            requiresSaving = true;
            LOGGER.info("Config schema version {} is outdated, updated to version {} via data fixer.", schemaVersion, ControlifyDataFixer.CURRENT_VERSION);
        }

        DataResult<ControlifyConfig> dtoResult = ControlifyConfig.CODEC.parse(fixed);
        if (dtoResult.isError()) {
            String errorMessage = dtoResult.error().map(DataResult.Error::message).orElse("");
            LOGGER.warn("Failed to decode config DTO as-read, attempting to complete missing values with defaults: {}", errorMessage);

            JsonObject fixedJson = (JsonObject) fixed.getValue();
            JsonObject completedJson = this.tryCompleteConfig(fixedJson);

            DataResult<ControlifyConfig> retryResult = ControlifyConfig.CODEC.parse(JsonOps.INSTANCE, completedJson);
            if (retryResult.isError()) {
                String retryErrorMessage = retryResult.error().map(DataResult.Error::message).orElse("");
                throw new IOException("Failed to decode config DTO after attempting to complete missing values.\nOriginal error: " + errorMessage + "\nRetry error: " + retryErrorMessage);
            }
            dtoResult = retryResult;
            requiresSaving = true;
            LOGGER.warn("Successfully decoded config DTO after completing missing values with defaults.");
        }
        ControlifyConfig dto = dtoResult.result().orElseThrow(() -> new IOException("Failed to decode config DTO"));

        this.settings = ControlifySettings.fromDTO(dto);

        if (requiresSaving) {
            LOGGER.info("Config required completion of missing values, saving updated config.");
            this.saveSafely();
        } else {
            LOGGER.info("Config loaded successfully from {}", this.path.toAbsolutePath());
        }

        return true;
    }

    public boolean save() throws IOException {
        LOGGER.info("Saving Controlify config to {}", this.path.toAbsolutePath());

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

        String jsonString = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()
                .toJson(jsonObject);

        Files.writeString(this.path, jsonString);
        LOGGER.info("Config saved successfully");
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

    private void makeBackup() {
        try {
            int backupIndex = 0;
            Path backupPath;
            do {
                backupPath = this.path.resolveSibling(this.path.getFileName() + ".backup" + (backupIndex == 0 ? "" : backupIndex));
                backupIndex++;
            } while (Files.exists(backupPath));

            Files.copy(this.path, backupPath);
            LOGGER.info("Created config backup at {}", backupPath.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to create config backup", e);
        }
    }

    // Attempts to complete any missing config values with defaults
    // In the case of a modpack providing a partial config file
    private JsonObject tryCompleteConfig(JsonObject jsonObject) {
        ControlifySettings defaultSettings = ControlifySettings.defaults();
        JsonObject defaultJson = ControlifyConfig.CODEC
                .encodeStart(JsonOps.INSTANCE, defaultSettings.toDTO())
                .result()
                .orElseThrow(() -> new IllegalStateException("Failed to encode default config DTO"))
                .getAsJsonObject();

        JsonObject mergedJson = defaultJson.deepCopy();
        DefaultConfigManager.mergeJsonObjects(mergedJson, jsonObject);

        return mergedJson;
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
