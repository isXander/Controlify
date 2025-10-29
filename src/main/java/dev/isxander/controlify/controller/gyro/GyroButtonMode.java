package dev.isxander.controlify.controller.gyro;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GyroButtonMode implements StringRepresentable {
    ON,
    INVERT,
    TOGGLE,
    OFF;

    public static final Codec<GyroButtonMode> CODEC = StringRepresentable.fromEnum(GyroButtonMode::values);

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
