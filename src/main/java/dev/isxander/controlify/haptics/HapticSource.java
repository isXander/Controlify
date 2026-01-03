package dev.isxander.controlify.haptics;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;

import java.util.*;

public record HapticSource(Identifier id) {
    public static final Codec<HapticSource> CODEC = Identifier.CODEC
            .xmap(HapticSource::get, HapticSource::id);

    private static final Map<Identifier, HapticSource> SOURCES = new Object2ObjectLinkedOpenHashMap<>();

    public static final HapticSource
            MASTER = register("master"),
            PLAYER = register("player"),
            WORLD = register("world"),
            INTERACTION = register("interaction"),
            GUI = register("gui");

    public static HapticSource get(Identifier id) {
        HapticSource source = SOURCES.get(id);
        if (source == null) {
            CUtil.LOGGER.warn("Unknown rumble source: {}. Using master.", id);
            return MASTER;
        }
        return source;
    }

    public static Collection<HapticSource> values() {
        return SOURCES.values();
    }

    public static JsonObject getDefaultJson() {
        JsonObject object = new JsonObject();
        for (HapticSource source : SOURCES.values()) {
            object.addProperty(source.id().toString(), 1f);
        }
        return object;
    }

    public static Map<Identifier, Float> getDefaultMap() {
        Map<Identifier, Float> map = new HashMap<>();

        for (HapticSource source : SOURCES.values()) {
            map.put(source.id(), 1f);
        }

        return map;
    }

    public static HapticSource register(Identifier id) {
        var source = new HapticSource(id);
        SOURCES.put(id, source);
        return source;
    }

    public static HapticSource register(String identifier, String path) {
        return register(Identifier.fromNamespaceAndPath(identifier, path));
    }

    private static HapticSource register(String path) {
        return register("controlify", path);
    }
}
