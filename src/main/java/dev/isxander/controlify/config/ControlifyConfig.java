package dev.isxander.controlify.config;

import com.google.gson.*;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ControlifyConfig {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("controlify.json");

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static JsonObject config = new JsonObject();

    public static void save() {
        try {
            generateConfig();

            Files.deleteIfExists(CONFIG_PATH);
            Files.writeString(CONFIG_PATH, GSON.toJson(config), StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config!", e);
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            applyConfig(GSON.fromJson(Files.readString(CONFIG_PATH), JsonObject.class));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config!", e);
        }
    }

    private static void generateConfig() {
        JsonObject configCopy = config.deepCopy(); // we use the old config, so we don't lose disconnected controller data

        for (var controller : Controller.CONTROLLERS.values()) {
            // `add` replaces if already existing
            configCopy.add(controller.guid(), generateControllerConfig(controller));
        }

        config = configCopy;
    }

    private static JsonObject generateControllerConfig(Controller controller) {
        JsonObject object = new JsonObject();

        object.add("config", GSON.toJsonTree(controller.config()));
        object.add("bindings", controller.bindings().toJson());

        return object;
    }

    private static void applyConfig(JsonObject object) {
        for (var controller : Controller.CONTROLLERS.values()) {
            var settings = object.getAsJsonObject(controller.guid());
            if (settings != null) {
                applyControllerConfig(controller, settings);
            }
        }
    }

    private static void applyControllerConfig(Controller controller, JsonObject object) {
        controller.config().overwrite(GSON.fromJson(object.getAsJsonObject("config"), ControllerConfig.class));
        controller.bindings().fromJson(object.getAsJsonObject("bindings"));
    }
}
