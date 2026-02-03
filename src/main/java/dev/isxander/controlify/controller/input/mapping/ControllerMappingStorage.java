package dev.isxander.controlify.controller.input.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class ControllerMappingStorage {
    private static final Map<String, ControllerMapping> MAPPINGS = new Object2ObjectOpenHashMap<>();

    public static @Nullable ControllerMapping get(String id) {
        return MAPPINGS.computeIfAbsent(id, ControllerMappingStorage::resolve);
    }

    private static @Nullable ControllerMapping resolve(String id) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Resource resource = resourceManager
                .getResource(CUtil.rl("mappings/" + id + ".json"))
                .orElse(null);
        if (resource == null)
            return null;

        try (BufferedReader reader = resource.openAsReader()) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            DataResult<ControllerMapping> result = ControllerMapping.CODEC.parse(JsonOps.INSTANCE, jsonElement);
            return result.getOrThrow();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load controller mapping!", e);
        }
    }
}
