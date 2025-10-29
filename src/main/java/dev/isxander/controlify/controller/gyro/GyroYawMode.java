package dev.isxander.controlify.controller.gyro;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GyroYawMode implements StringRepresentable {
    YAW,
    ROLL,
    BOTH;

    public static final Codec<GyroYawMode> CODEC = StringRepresentable.fromEnum(GyroYawMode::values);

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
