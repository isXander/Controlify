package dev.isxander.controlify.config;

import com.google.gson.*;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.mapping.MappingEntry;
import dev.isxander.controlify.controller.input.mapping.MappingEntryTypeAdapter;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.DebugLog;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

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
    // responsible citizens will set dirty so the config can only re-save when needed
    private boolean dirty;
    private boolean firstLaunch;

    private String currentControllerUid = null;
    // used so saving the config doesn't lose controller config that isn't currently connected
    // key: controller uid
    private final Map<String, JsonObject> storedControllerConfig = new HashMap<>();
    private @NotNull GlobalSettings globalSettings = new GlobalSettings();

    public ControlifyConfig(Controlify controlify) {
        this.controlify = controlify;
    }

    public void save() {
        CUtil.LOGGER.log("Saving Controlify config...");

        JsonObject serialObject;
        try {
            serialObject = createSerialObject();
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to serialize Controlify config. Controlify will not be saved!", e);
            return;
        }

        try {
            Files.deleteIfExists(CONFIG_PATH);
            Files.writeString(
                    CONFIG_PATH,
                    GSON.toJson(serialObject),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING
            );
            dirty = false;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save Controlify config to file!", e);
        }
    }

    public void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    public void load() {
        CUtil.LOGGER.log("Loading Controlify config...");

        if (!Files.exists(CONFIG_PATH)) {
            CUtil.LOGGER.log("First launch detected. Creating initial config file!");
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

    private JsonObject createSerialObject() {
        JsonObject obj = new JsonObject();

        { // Current controller
            obj.addProperty(
                    "current_controller",
                    controlify.getCurrentController().map(ControllerEntity::uid).orElse(null)
            );
        }

        { // Controller config
            controlify.getControllerManager().ifPresent(this::updateStoredControllerConfig);
            JsonObject controllersObj = new JsonObject();
            storedControllerConfig.forEach(controllersObj::add);
            obj.add("controllers", controllersObj);
        }

        { // Global settings
            JsonElement globalJson = GSON.toJsonTree(globalSettings);
            obj.add("global", globalJson);
        }

        return obj;
    }

    private void updateStoredControllerConfig(ControllerManager controllerManager) {
        for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
            // get the existing config to modify
            JsonObject controllerObject = storedControllerConfig
                    .computeIfAbsent(controller.uid(), k -> new JsonObject());

            // get config object within that object, or create and add it
            JsonObject configObject = controllerObject.getAsJsonObject("config");
            if (configObject == null) {
                configObject = new JsonObject();
                controllerObject.add("config", configObject);
            }

            // now the serialization will not remove objects that it doesn't need
            // this is useful because hd haptics only applies if controller is wired,
            // so if the user interchanges between BT+W it won't lose the HD haptic config
            controller.serializeToObject(configObject, GSON);

            storedControllerConfig.put(controller.uid(), controllerObject);
        }
    }

    private void applyConfig(JsonObject json) {
        try {
            JsonElement primitive = json.get("current_controller");
            if (primitive != null) {
                currentControllerUid = primitive.isJsonNull() ? null : primitive.getAsString();
            } else {
                CUtil.LOGGER.warn("Current controller is not defined in config!");
                setDirty();
            }
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to apply current controller from config file!", e);
            setDirty();
        }

        try {
            JsonObject controllersMap = json.getAsJsonObject("controllers");
            controllersMap.asMap().forEach((uid, element) -> {
                storedControllerConfig.put(uid, element.getAsJsonObject());
            });
            controlify.getControllerManager().ifPresent(this::applyControllerConfig);
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to apply controller config from config file!", e);
            setDirty();
        }

        try {
            GlobalSettings newGlobalSettings = GSON.fromJson(json.get("global"), GlobalSettings.class);
            if (newGlobalSettings != null) globalSettings = newGlobalSettings;
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to apply global settings from config file!", e);
            setDirty();
        }
    }

    private void applyControllerConfig(ControllerManager controllerManager) {
        for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
            loadControllerConfig(controller);
        }
    }

    public boolean loadControllerConfig(ControllerEntity controller) {
        JsonObject json = storedControllerConfig.get(controller.uid());

        if (json == null) {
            CUtil.LOGGER.warn("Controller {} has no config to load. Using defaults.", controller.info().ucid());
            setDirty();
            return true;
        }

        JsonObject innerJson = json.getAsJsonObject("config");

        try {
            controller.deserializeFromObject(innerJson.deepCopy(), GSON);
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to load controller {} config!", controller.info().ucid(), e);
            setDirty();
        }

        return false;
    }

    public @Nullable String currentControllerUid() {
        return this.currentControllerUid;
    }

    public @NotNull GlobalSettings globalSettings() {
        return this.globalSettings;
    }

    public boolean isFirstLaunch() {
        return this.firstLaunch;
    }

    public void setDirty() {
        dirty = true;
    }
}
