package dev.isxander.controlify.controller;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.hid.HIDIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.quiltmc.json5.JsonReader;

import java.io.IOException;
import java.util.*;

public class ControllerType {
    public static final ControllerType UNKNOWN = new ControllerType("Unknown", "unknown");

    private static Map<HIDIdentifier, ControllerType> typeMap = null;
    private static final ResourceLocation hidDbLocation = new ResourceLocation("controlify", "controllers/controller_identification.json5");

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
                    Controlify.LOGGER.error("Failed to load HID DB from source", e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        var type = typeMap.getOrDefault(hid, UNKNOWN);
        if (type == UNKNOWN) {
            Controlify.LOGGER.warn("Controller type unknown! Please report the make and model of your controller and give the following details: " + hid);
        }
        return type;
    }

    private static void readControllerIdFiles(JsonReader reader) throws IOException {
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
    }
}
