package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.hid.HIDIdentifier;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public enum ControllerType {
    UNKNOWN("Unknown Controller", ControllerTheme.XBOX_ONE),
    XBOX_ONE("Xbox Controller", ControllerTheme.XBOX_ONE),
    XBOX_360("Xbox 360 Controller", ControllerTheme.XBOX_ONE),
    DUALSHOCK4("PS4 Controller", ControllerTheme.DUALSHOCK4);

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static Map<HIDIdentifier, ControllerType> typeMap = null;

    private final String friendlyName;
    private final ControllerTheme theme;

    ControllerType(String friendlyName, ControllerTheme theme) {
        this.friendlyName = friendlyName;
        this.theme = theme;
    }

    public String friendlyName() {
        return friendlyName;
    }

    public ControllerTheme theme() {
        return theme;
    }

    public static ControllerType getTypeForHID(HIDIdentifier hid) {
        if (typeMap != null) return typeMap.getOrDefault(hid, UNKNOWN);

        typeMap = new HashMap<>();
        try {
            try (var hidDb = ControllerType.class.getResourceAsStream("/hiddb.json5")) {
                var json = GSON.fromJson(new InputStreamReader(hidDb), JsonObject.class);
                for (var type : ControllerType.values()) {
                    if (!json.has(type.name().toLowerCase())) continue;

                    var themeJson = json.getAsJsonObject(type.name().toLowerCase());

                    int vendorId = themeJson.get("vendor").getAsInt();
                    for (var productIdEntry : themeJson.getAsJsonArray("product")) {
                        int productId = productIdEntry.getAsInt();
                        typeMap.put(new HIDIdentifier(vendorId, productId), type);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return typeMap.getOrDefault(hid, UNKNOWN);
    }
}
