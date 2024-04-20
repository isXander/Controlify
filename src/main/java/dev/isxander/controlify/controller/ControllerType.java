package dev.isxander.controlify.controller;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.JsonTreeParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.gson.GsonReader;

import java.util.*;

public record ControllerType(@Nullable String friendlyName, String mappingId, String namespace, boolean forceJoystick, boolean dontLoad) {
    public static final ControllerType UNKNOWN = new ControllerType(null, "unknown", "unknown", false, false);

    public static final MapCodec<ControllerType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", null).forGetter(ControllerType::friendlyName),
            Codec.STRING.optionalFieldOf("mapping", "unmapped").forGetter(ControllerType::mappingId),
            Codec.STRING.optionalFieldOf("theme", "unknown").forGetter(ControllerType::namespace),
            Codec.BOOL.optionalFieldOf("force_joystick", false).forGetter(ControllerType::forceJoystick),
            Codec.BOOL.optionalFieldOf("dont_load", false).forGetter(ControllerType::dontLoad)
    ).apply(instance, ControllerType::new));
    private static final Codec<ControllerTypeEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(HIDIdentifier.LIST_CODEC)
                    .comapFlatMap(list -> list.isEmpty() ? DataResult.error(() -> "At least one HID must be present") : DataResult.success(list), list -> list)
                    .fieldOf("hids")
                    .forGetter(ControllerTypeEntry::hid),
            CODEC.forGetter(ControllerTypeEntry::type)
    ).apply(instance, ControllerTypeEntry::new));

    private static final Gson GSON = new GsonBuilder()
            .setNumberToNumberStrategy(com.google.gson.stream.JsonReader::nextInt)
            .create();

    private record ControllerTypeEntry(List<HIDIdentifier> hid, ControllerType type) {}

    private static Map<HIDIdentifier, ControllerType> typeMap = null;
    private static final ResourceLocation hidDbLocation = new ResourceLocation("controlify", "controllers/controller_identification.json5");

    public ResourceLocation getIconSprite() {
        /*? if >=1.20.3 {*/
        return Controlify.id("controllers/" + namespace);
        /*?} else {*//*
        return Controlify.id("textures/gui/sprites/controllers/" + namespace + ".png");
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
                    var reader = JsonReader.json5(resourceReader);
                    JsonElement json = JsonTreeParser.parse(reader);
                    ENTRY_CODEC.listOf().parse(JsonOps.INSTANCE, json)
                            .resultOrPartial(CUtil.LOGGER::error)
                            .ifPresent(entries -> {
                                for (var entry : entries) {
                                    for (var hid : entry.hid()) {
                                        typeMap.put(hid, entry.type());
                                    }
                                }
                            });
                } catch (Exception e) {
                    CUtil.LOGGER.error("Failed to load HID DB from source", e);
                }
            }
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to load HID DB from source", e);
        }
    }

    public static ImmutableMap<HIDIdentifier, ControllerType> getTypeMap() {
        ensureTypeMapFilled();
        return ImmutableMap.copyOf(typeMap);
    }
}
