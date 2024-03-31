package dev.isxander.controlify.controller;

import com.google.common.collect.ImmutableMap;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.JsonReader;

import java.io.IOException;
import java.util.*;

public record ControllerType(@Nullable String friendlyName, String mappingId, String namespace, boolean forceJoystick, boolean dontLoad) {
    public static final ControllerType UNKNOWN = new ControllerType(null, "unknown", "unknown", false, false);

    private static Map<HIDIdentifier, ControllerType> typeMap = null;
    private static final ResourceLocation hidDbLocation = new ResourceLocation("controlify", "controllers/controller_identification.json5");

    public ResourceLocation getIconSprite() {
        /*? if >=1.20.3 {*/
        return Controlify.id("inputs/" + namespace + "/icon");
        /*?} else {*//*
        return Controlify.id("textures/gui/sprites/inputs/" + namespace + "/icon.png");
        *//*?} */

    }

    public static ControllerType getTypeForHID(HIDIdentifier hid) {
        if (getTypeMap().containsKey(hid)) {
            return getTypeMap().get(hid);
        } else {
            CUtil.LOGGER.warn("Controller found via USB hardware scan, but it was not found in the controller identification database! (HID: {})", hid);
            return ControllerType.UNKNOWN;
        }
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
                    CUtil.LOGGER.error("Failed to load HID DB from source", e);
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
            String namespace = "unknown";
            String mappingId = "unmapped";
            boolean forceJoystick = false;
            boolean dontLoad = false;
            Set<HIDIdentifier> hids = new HashSet<>();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "name" -> friendlyName = reader.nextString();
                    case "identifier" -> legacyIdentifier = reader.nextString();
                    case "theme" -> namespace = reader.nextString();
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
                                    CUtil.LOGGER.warn("Too many values in HID array. Skipping...");
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
                        CUtil.LOGGER.warn("Unknown key in HID DB: " + name + ". Skipping...");
                        reader.skipValue();
                    }
                }
            }
            reader.endObject();

            if (legacyIdentifier != null) {
                CUtil.LOGGER.warn("Legacy identifier found in HID DB. Please replace with `theme` and `mapping` (if needed).");
                namespace = legacyIdentifier;
                mappingId = legacyIdentifier;
            }

            if (hids.isEmpty()) {
                CUtil.LOGGER.warn("HID DB entry does not specify any VID/PID. Skipping...");
                continue;
            }

            var type = new ControllerType(friendlyName, mappingId, namespace, forceJoystick, dontLoad);
            for (HIDIdentifier hid : hids) {
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
