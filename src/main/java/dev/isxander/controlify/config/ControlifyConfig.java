package dev.isxander.controlify.config;

import com.google.gson.*;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.mapping.MappingEntry;
import dev.isxander.controlify.controller.input.mapping.MappingEntryTypeAdapter;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.DebugLog;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ToastUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ControlifyConfig {
    public static final Path CONFIG_PATH = PlatformMainUtil.getConfigDir().resolve("controlify.json");

    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeHierarchyAdapter(Class.class, new TypeAdapters.ClassTypeAdapter())
            .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(MappingEntry.class, new MappingEntryTypeAdapter()) // not hierarchy!! otherwise stackoverflow when using default gson record deserializer
            .create();

    private final Controlify controlify;

    private String currentControllerUid;
    private JsonObject controllerData = new JsonObject();
    private GlobalSettings globalSettings = new GlobalSettings();
    private boolean firstLaunch;

    private boolean dirty;

    public ControlifyConfig(Controlify controlify) {
        this.controlify = controlify;
    }

    public void save() {
        CUtil.LOGGER.info("Saving Controlify config...");

        try {
            Files.deleteIfExists(CONFIG_PATH);
            Files.writeString(CONFIG_PATH, GSON.toJson(generateConfig()), StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);
            dirty = false;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config!", e);
        }
    }

    public void load() {
        CUtil.LOGGER.info("Loading Controlify config...");

        if (!Files.exists(CONFIG_PATH)) {
            firstLaunch = true;
            save();
            return;
        }

        try {
            applyConfig(GSON.fromJson(Files.readString(CONFIG_PATH), JsonObject.class));
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to load Controlify config!", e);
        }

        if (dirty) {
            DebugLog.log("Config was dirty after load, saving...");
            save();
        }
    }

    private JsonObject generateConfig() {
        JsonObject config = new JsonObject();

        JsonObject newControllerData = controllerData.deepCopy(); // we use the old config, so we don't lose disconnected controller data

        controlify.getControllerManager().ifPresent(controllerManager -> {
            for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
                // `add` replaces if already existing
                newControllerData.add(controller.info().uid(), generateControllerConfig(controller));
            }
        });

        controllerData = newControllerData;
        config.addProperty("current_controller", currentControllerUid = controlify.getCurrentController().map(c -> c.info().uid()).orElse(null));
        config.add("controllers", controllerData);
        config.add("global", GSON.toJsonTree(globalSettings));

        return config;
    }

    private JsonObject generateControllerConfig(ControllerEntity controller) {
        JsonObject object = new JsonObject();
        JsonObject config = new JsonObject();
        controller.serializeToObject(config, GSON);

        object.add("config", config);

        return object;
    }

    private void applyConfig(JsonObject object) {
        globalSettings = GSON.fromJson(object.getAsJsonObject("global"), GlobalSettings.class);
        if (globalSettings == null) {
            globalSettings = new GlobalSettings();
            setDirty();
        }

        JsonObject controllers = object.getAsJsonObject("controllers");
        if (controllers != null) {
            this.controllerData = controllers;
            if (controlify.getControllerManager().isPresent()) {
                for (var controller : controlify.getControllerManager().get().getConnectedControllers()) {
                    loadOrCreateControllerData(controller);
                }
            }
        } else {
            setDirty();
        }

        if (object.has("current_controller")) {
            JsonElement element = object.get("current_controller");
            currentControllerUid = element.isJsonNull() ? null : element.getAsString();
        } else {
            currentControllerUid = controlify.getCurrentController().map(c -> c.info().uid()).orElse(null);
            setDirty();
        }
    }

    public boolean loadOrCreateControllerData(ControllerEntity controller) {
        var uid = controller.info().uid();
        if (controllerData.has(uid)) {
            DebugLog.log("Loading controller data for " + uid);
            applyControllerConfig(controller, controllerData.getAsJsonObject(uid));
            return true;
        } else {
            DebugLog.log("New controller found, setting config dirty ({})", uid);
            setDirty();
            return false;
        }
    }

    private void applyControllerConfig(ControllerEntity controller, JsonObject object) {
        try {
            controller.deserializeFromObject(object.getAsJsonObject("config"), GSON);
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to load controller data for {}. Resetting to default!", controller.info().uid(), e);
            controller.resetToDefaultConfig();
            ToastUtils.sendToast(Component.translatable("controlify.toast.fail_conf_load.title"), Component.translatable("controlify.toast.fail_conf_load.desc"), true);
            save();
        }
    }

    public void setDirty() {
        dirty = true;
    }

    public void saveIfDirty() {
        if (dirty) {
            DebugLog.log("Config is dirty. Saving...");
            save();
        }
    }

    public GlobalSettings globalSettings() {
        return globalSettings;
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    @Nullable
    public String currentControllerUid() {
        return currentControllerUid;
    }
}
