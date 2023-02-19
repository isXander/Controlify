package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.hid.HIDIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.function.IOSupplier;
import org.quiltmc.json5.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
            List<PackResources> packs = Minecraft.getInstance().getResourceManager().listPacks().toList();

            for (var pack : packs) {
                String packName = pack.packId();
                IoSupplier<InputStream> isSupplier = pack.getResource(PackType.CLIENT_RESOURCES, hidDbLocation);
                if (isSupplier == null) continue;
                Controlify.LOGGER.info("Loading controller HID DB from pack " + packName);

                try (var hidDb = isSupplier.get()) {
                    JsonReader reader = JsonReader.json5(new InputStreamReader(hidDb));

                    reader.beginArray();
                    while (reader.hasNext()) {
                        String friendlyName = null;
                        String identifier = null;
                        int vendorId = -1;
                        Set<Integer> productIds = new HashSet<>();

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name = reader.nextName();

                            switch (name) {
                                case "name" -> friendlyName = reader.nextString();
                                case "identifier" -> identifier = reader.nextString();
                                case "vendor" -> vendorId = reader.nextInt();
                                case "product" -> {
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        productIds.add(reader.nextInt());
                                    }
                                    reader.endArray();
                                }
                                default -> {
                                    Controlify.LOGGER.warn("Unknown key in HID DB: " + name + ". Skipping...");
                                    reader.skipValue();
                                }
                            }
                        }
                        reader.endObject();

                        if (friendlyName == null || identifier == null || vendorId == -1 || productIds.isEmpty()) {
                            Controlify.LOGGER.warn("Invalid entry in HID DB. Skipping...");
                            continue;
                        }

                        var type = new ControllerType(friendlyName, identifier);
                        for (int productId : productIds) {
                            typeMap.put(new HIDIdentifier(vendorId, productId), type);
                        }
                    }
                    reader.endArray();
                } catch (Exception e) {
                    Controlify.LOGGER.error("Failed to load HID DB from pack " + packName, e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return typeMap.getOrDefault(hid, UNKNOWN);
    }
}
