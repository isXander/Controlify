package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import dev.isxander.controlify.controller.hid.HIDIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ControllerType {
    public static final ControllerType UNKNOWN = new ControllerType("Unknown", "unknown");

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static Map<HIDIdentifier, ControllerType> typeMap = null;
    private static final ResourceLocation hidDbLocation = new ResourceLocation("controlify", "hiddb.json5");

    private final String friendlyName;
    private final String identifier;

    private ControllerType(String friendlyName, String identifier) {
        this.friendlyName = friendlyName;
        this.identifier = identifier;
    }

    public String friendlyName() {
        return friendlyName;
    }

    public String identifier() {
        return identifier;
    }

    public static ControllerType getTypeForHID(HIDIdentifier hid) {
        if (typeMap != null) return typeMap.getOrDefault(hid, UNKNOWN);

        typeMap = new HashMap<>();
        try {
            List<IoSupplier<InputStream>> dbs = Minecraft.getInstance().getResourceManager().listPacks()
                    .map(pack -> pack.getResource(PackType.CLIENT_RESOURCES, hidDbLocation))
                    .filter(Objects::nonNull)
                    .toList();

            for (var supplier : dbs) {
                try (var hidDb = supplier.get()) {
                    var json = GSON.fromJson(new InputStreamReader(hidDb), JsonArray.class);
                    for (var typeElement : json) {
                        var typeObject = typeElement.getAsJsonObject();

                        ControllerType type = new ControllerType(typeObject.get("name").getAsString(), typeObject.get("identifier").getAsString());

                        int vendorId = typeObject.get("vendor").getAsInt();
                        for (var productIdEntry : typeObject.getAsJsonArray("product")) {
                            int productId = productIdEntry.getAsInt();
                            typeMap.put(new HIDIdentifier(vendorId, productId), type);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return typeMap.getOrDefault(hid, UNKNOWN);
    }
}
