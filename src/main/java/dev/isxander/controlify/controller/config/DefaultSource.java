package dev.isxander.controlify.controller.config;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface DefaultSource {
    JsonObject createDefaultConfig(ResourceLocation id);
}
