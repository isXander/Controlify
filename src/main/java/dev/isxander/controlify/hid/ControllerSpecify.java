package dev.isxander.controlify.hid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ControllerSpecify {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = PlatformMainUtil.getConfigDir().resolve("controlify-controllerspecify.json");

    private static ControllerSpecify instance;

    private String assignedControllerMac = "";

    public static ControllerSpecify get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static ControllerSpecify load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ControllerSpecify loaded = GSON.fromJson(json, ControllerSpecify.class);
                if (loaded != null) {
                    CUtil.LOGGER.log("Loaded ControllerSpecify config: assignedControllerMac='{}'", loaded.assignedControllerMac);
                    return loaded;
                }
            } catch (IOException e) {
                CUtil.LOGGER.error("Failed to read controlify-controllerspecify.json, using default", e);
            }
        }

        ControllerSpecify fresh = new ControllerSpecify();
        fresh.save();
        CUtil.LOGGER.log("Created default controlify-controllerspecify.json at {} - set assignedControllerMac to restrict this instance to one controller", CONFIG_PATH);
        return fresh;
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            CUtil.LOGGER.error("Failed to write controlify-controllerspecify.json", e);
        }
    }

    public boolean isFilteringEnabled() {
        return assignedControllerMac != null && !assignedControllerMac.isBlank();
    }

    public String getAssignedControllerMac() {
        return assignedControllerMac;
    }

    public void setAssignedControllerMac(String mac) {
        this.assignedControllerMac = mac;
        save();
    }

    public static String normalizeMac(String mac) {
        if (mac == null) return "";
        return mac.replace(":", "").replace("-", "").trim().toLowerCase(Locale.ROOT);
    }
}
