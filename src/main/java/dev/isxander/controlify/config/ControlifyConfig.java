package dev.isxander.controlify.config;

import com.google.gson.*;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
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
            .registerTypeHierarchyAdapter(Class.class, new ClassTypeAdapter())
            .create();

    private JsonObject controllerData = new JsonObject();
    private GlobalSettings globalSettings = new GlobalSettings();
    private boolean firstLaunch;

    public void save() {
        Controlify.LOGGER.info("Saving Controlify config...");

        try {
            Files.deleteIfExists(CONFIG_PATH);
            Files.writeString(CONFIG_PATH, GSON.toJson(generateConfig()), StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config!", e);
        }
    }

    public void load() {
        Controlify.LOGGER.info("Loading Controlify config...");

        if (!Files.exists(CONFIG_PATH)) {
            firstLaunch = true;
            save();
            return;
        }

        try {
            applyConfig(GSON.fromJson(Files.readString(CONFIG_PATH), JsonObject.class));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config!", e);
        }
    }

    private JsonObject generateConfig() {
        JsonObject config = new JsonObject();

        JsonObject newControllerData = controllerData.deepCopy(); // we use the old config, so we don't lose disconnected controller data

        for (var controller : Controller.CONTROLLERS.values()) {
            // `add` replaces if already existing
            newControllerData.add(controller.uid(), generateControllerConfig(controller));
        }

        controllerData = newControllerData;
        config.add("controllers", controllerData);

        config.add("global", GSON.toJsonTree(globalSettings));

        return config;
    }

    private JsonObject generateControllerConfig(Controller<?, ?> controller) {
        JsonObject object = new JsonObject();

        object.add("config", GSON.toJsonTree(controller.config()));
        object.add("bindings", controller.bindings().toJson());

        return object;
    }

    private void applyConfig(JsonObject object) {
        globalSettings = GSON.fromJson(object.getAsJsonObject("global"), GlobalSettings.class);
        if (globalSettings == null) globalSettings = new GlobalSettings();

        JsonObject controllers = object.getAsJsonObject("controllers");
        if (controllers != null) {
            this.controllerData = controllers;
            for (var controller : Controller.CONTROLLERS.values()) {
                loadOrCreateControllerData(controller);
            }
        }
    }

    public boolean loadOrCreateControllerData(Controller<?, ?> controller) {
        var uid = controller.uid();
        if (controllerData.has(uid)) {
            Controlify.LOGGER.info("Loading controller data for " + uid);
            applyControllerConfig(controller, controllerData.getAsJsonObject(uid));
            return true;
        } else {
            Controlify.LOGGER.info("New controller found, creating controller data for " + uid);
            save();
            return false;
        }
    }

    private void applyControllerConfig(Controller<?, ?> controller, JsonObject object) {
        try {
            controller.setConfig(GSON, object.getAsJsonObject("config"));
            controller.bindings().fromJson(object.getAsJsonObject("bindings"));
        } catch (Exception e) {
            Controlify.LOGGER.error("Failed to load controller data for " + controller.uid() + ". Resetting to default!", e);
            controller.resetConfig();
            save();
        }
    }

    public GlobalSettings globalSettings() {
        return globalSettings;
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }
}
