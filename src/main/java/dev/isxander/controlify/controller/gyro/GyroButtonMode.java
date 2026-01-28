package dev.isxander.controlify.controller.gyro;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum GyroButtonMode implements StringRepresentable {
    ON,
    INVERT,
    TOGGLE,
    OFF;

    public static final Codec<GyroButtonMode> CODEC = StringRepresentable.fromEnum(GyroButtonMode::values);

    @Override
    public @NonNull String getSerializedName() {
        return this.name().toLowerCase();
    }
}
