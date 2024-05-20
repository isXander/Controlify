package dev.isxander.controlify.bindings.defaults;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.bindings.input.Input;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record MapBackedDefaultBindProvider(Map<ResourceLocation, Input> map) implements DefaultBindProvider {
    public static final Codec<MapBackedDefaultBindProvider> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Input.CODEC)
            .xmap(MapBackedDefaultBindProvider::new, MapBackedDefaultBindProvider::map);

    @Override
    public @Nullable Input getDefaultBind(ResourceLocation bindId) {
        return map.get(bindId);
    }
}
