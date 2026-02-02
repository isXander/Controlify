package dev.isxander.controlify.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.config.dto.device.GyroCalibrationConfig;
import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.controller.gyro.GyroYawMode;

public record GyroConfig(
        float lookSensitivity,
        boolean relativeMode,
        boolean invertPitch,
        boolean invertYaw,
        GyroButtonMode buttonMode,
        GyroYawMode yawMode,
        boolean flickStick
) {
    public static final Codec<GyroConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("look_sensitivity").forGetter(GyroConfig::lookSensitivity),
            Codec.BOOL.fieldOf("relative_mode").forGetter(GyroConfig::relativeMode),
            Codec.BOOL.fieldOf("invert_pitch").forGetter(GyroConfig::invertPitch),
            Codec.BOOL.fieldOf("invert_yaw").forGetter(GyroConfig::invertYaw),
            GyroButtonMode.CODEC.fieldOf("button_mode").forGetter(GyroConfig::buttonMode),
            GyroYawMode.CODEC.fieldOf("yaw_mode").forGetter(GyroConfig::yawMode),
            Codec.BOOL.fieldOf("flick_stick").forGetter(GyroConfig::flickStick)
    ).apply(instance, GyroConfig::new));

}
