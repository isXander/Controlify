package dev.isxander.controlify.bindings.defaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.bindings.input.Input;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

public record MapBackedDefaultBindProvider(Map<ResourceLocation, Input> map) implements DefaultBindProvider {
    public static final MapCodec<MapBackedDefaultBindProvider> MAP_CODEC = Codec.simpleMap(
            ResourceLocation.CODEC, Input.CODEC,
            Keyable.forStrings(() -> ControlifyBindApi.get().getAllBindIds().map(ResourceLocation::toString))
    ).xmap(MapBackedDefaultBindProvider::new, MapBackedDefaultBindProvider::map);

    @Override
    public @Nullable Input getDefaultBind(ResourceLocation bindId) {
        return map.get(bindId);
    }
}
