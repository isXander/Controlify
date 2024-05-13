package dev.isxander.controlify.bindings.v2.defaults;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.bindings.Bind;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record MapBackedDefaultBindProvider(Map<ResourceLocation, Bind> map) implements DefaultBindProvider {
    public static final Codec<MapBackedDefaultBindProvider> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Bind.CODEC)
            .xmap(MapBackedDefaultBindProvider::new, MapBackedDefaultBindProvider::map);

    @Override
    public @Nullable Bind getDefaultBind(ResourceLocation bindId) {
        return map.get(bindId);
    }
}
