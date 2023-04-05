package dev.isxander.controlify.rumble;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RumbleSource {
    private static final Map<ResourceLocation, RumbleSource> SOURCES = new LinkedHashMap<>();

    public static final RumbleSource
            MASTER = register("master"),
            DAMAGE = register("damage"),
            BLOCK_DESTROY = register("block_destroy"),
            USE_ITEM = register("use_item"),
            ITEM_BREAK = register("item_break"),
            GUI = register("gui"),
            GLOBAL_EVENT = register("global_event");

    private final ResourceLocation id;

    private RumbleSource(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
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

    public static RumbleSource register(ResourceLocation id) {
        var source = new RumbleSource(id);
        SOURCES.put(id, source);
        return source;
    }

    public static RumbleSource register(String identifier, String path) {
        return register(new ResourceLocation(identifier, path));
    }

    private static RumbleSource register(String path) {
        return register("controlify", path);
    }
}
