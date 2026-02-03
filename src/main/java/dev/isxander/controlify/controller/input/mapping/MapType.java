package dev.isxander.controlify.controller.input.mapping;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum MapType implements StringRepresentable {
    BUTTON,
    AXIS,
    HAT,
    NOTHING;

    public static final Codec<MapType> CODEC = StringRepresentable.fromEnum(MapType::values);

    @Override
    public @NonNull String getSerializedName() {
        return name().toLowerCase();
    }
}
