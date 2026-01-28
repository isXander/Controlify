package dev.isxander.controlify.controller.gyro;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum GyroYawMode implements StringRepresentable {
    YAW,
    ROLL,
    BOTH;

    public static final Codec<GyroYawMode> CODEC = StringRepresentable.fromEnum(GyroYawMode::values);

    @Override
    public @NonNull String getSerializedName() {
        return this.name().toLowerCase();
    }
}
