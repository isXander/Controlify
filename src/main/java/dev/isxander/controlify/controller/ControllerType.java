package dev.isxander.controlify.controller;

import com.google.common.collect.ImmutableMap;
import dev.isxander.controlify.controller.hid.HIDIdentifier;
import dev.isxander.controlify.utils.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.quiltmc.json5.JsonReader;

import java.io.IOException;
import java.util.*;

public record ControllerType(String friendlyName, String mappingId, String themeId, boolean forceJoystick, boolean dontLoad) {
    public static final ControllerType UNKNOWN = new ControllerType("Unknown", "unknown", "unknown", false, false);

    private static Map<HIDIdentifier, ControllerType> typeMap = null;
    private static final ResourceLocation hidDbLocation = new ResourceLocation("controlify", "controllers/controller_identification.json5");

    public static ControllerType getTypeForHID(HIDIdentifier hid) {
        return getTypeMap().getOrDefault(hid, ControllerType.UNKNOWN);
    }

    public static void ensureTypeMapFilled() {
        if (typeMap != null) return;

        typeMap = new HashMap<>();
        try {
            List<Resource> dbs = Minecraft.getInstance().getResourceManager()
                    .listResourceStacks("controllers", s -> s.equals(hidDbLocation)) // get the db file from every pack
                    .values().stream() // above ^^ function supports multiple resource locations so returns a map, we just want the one
                    .flatMap(Collection::stream) // flatten the list of list of resources
                    .toList();

            for (var resource : dbs) {
                try (var resourceReader = resource.openAsReader()) {
                    JsonReader reader = JsonReader.json5(resourceReader);
                    readControllerIdFiles(reader);

                } catch (Exception e) {
                    Log.LOGGER.error("Failed to load HID DB from source", e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readControllerIdFiles(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            String friendlyName = null;
            String legacyIdentifier = null;
            String themeId = null;
            String mappingId = null;
            boolean forceJoystick = false;
            boolean dontLoad = false;
            Set<HIDIdentifier> hids = new HashSet<>();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "name" -> friendlyName = reader.nextString();
                    case "identifier" -> legacyIdentifier = reader.nextString();
                    case "theme" -> themeId = reader.nextString();
                    case "mapping" -> mappingId = reader.nextString();
                    case "hids" -> {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            int vendorId = -1;
                            int productId = -1;
                            reader.beginArray();
                            while (reader.hasNext()) {
                                if (vendorId == -1) {
                                    vendorId = reader.nextInt();
                                } else if (productId == -1) {
                                    productId = reader.nextInt();
                                } else {
                                    Log.LOGGER.warn("Too many values in HID array. Skipping...");
                                    reader.skipValue();
                                }
                            }
                            reader.endArray();
                            hids.add(new HIDIdentifier(vendorId, productId));
                        }
                        reader.endArray();
                    }
                    case "force_joystick" -> forceJoystick = reader.nextBoolean();
                    case "dont_load" -> dontLoad = reader.nextBoolean();
                    default -> {
                        Log.LOGGER.warn("Unknown key in HID DB: " + name + ". Skipping...");
                        reader.skipValue();
                    }
                }
            }
            reader.endObject();

            if (legacyIdentifier != null) {
                Log.LOGGER.warn("Legacy identifier found in HID DB. Please replace with `theme` and `mapping` (if needed).");
                themeId = legacyIdentifier;
                mappingId = legacyIdentifier;
            }

            if (friendlyName == null || themeId == null || hids.isEmpty()) {
                Log.LOGGER.warn("Invalid entry in HID DB. Skipping...");
                continue;
            }

            var type = new ControllerType(friendlyName, mappingId, themeId, forceJoystick, dontLoad);
            for (var hid : hids) {
                typeMap.put(hid, type);
            }
        }
        reader.endArray();
    }

    public static ImmutableMap<HIDIdentifier, ControllerType> getTypeMap() {
        ensureTypeMapFilled();
        return ImmutableMap.copyOf(typeMap);
    }
}
