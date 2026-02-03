package dev.isxander.controlify.rumble;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;

import java.util.*;

public record RumbleSource(Identifier id) {
    public static final Codec<RumbleSource> CODEC = Identifier.CODEC
            .xmap(RumbleSource::get, RumbleSource::id);

    private static final Map<Identifier, RumbleSource> SOURCES = new Object2ObjectLinkedOpenHashMap<>();

    public static final RumbleSource
            MASTER = register("master"),
            PLAYER = register("player"),
            WORLD = register("world"),
            INTERACTION = register("interaction"),
            GUI = register("gui");

    public static RumbleSource get(Identifier id) {
        RumbleSource source = SOURCES.get(id);
        if (source == null) {
            CUtil.LOGGER.warn("Unknown rumble source: {}. Using master.", id);
            return MASTER;
        }
        return source;
    }

    public static Collection<RumbleSource> values() {
        return SOURCES.values();
    }

    public static JsonObject getDefaultJson() {
        JsonObject object = new JsonObject();
        for (RumbleSource source : SOURCES.values()) {
            object.addProperty(source.id().toString(), 1f);
        }
        return object;
    }

    public static Map<Identifier, Float> getDefaultMap() {
        Map<Identifier, Float> map = new HashMap<>();

        for (RumbleSource source : SOURCES.values()) {
            map.put(source.id(), 1f);
        }

        return map;
    }

    public static RumbleSource register(Identifier id) {
        var source = new RumbleSource(id);
        SOURCES.put(id, source);
        return source;
    }

    public static RumbleSource register(String identifier, String path) {
        return register(Identifier.fromNamespaceAndPath(identifier, path));
    }

    private static RumbleSource register(String path) {
        return register("controlify", path);
    }
}
