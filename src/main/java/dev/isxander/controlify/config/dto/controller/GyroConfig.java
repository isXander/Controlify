package dev.isxander.controlify.config.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.controller.gyro.GyroStateC;
import dev.isxander.controlify.controller.gyro.GyroYawMode;

public record GyroConfig(
        CalibrationConfig calibration,
        float lookSensitivity,
        boolean relativeMode,
        boolean invertPitch,
        boolean invertYaw,
        GyroButtonMode buttonMode,
        GyroYawMode yawMode
) {
    public static final Codec<GyroConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CalibrationConfig.CODEC.fieldOf("calibration").forGetter(GyroConfig::calibration),
            Codec.FLOAT.fieldOf("look_sensitivity").forGetter(GyroConfig::lookSensitivity),
            Codec.BOOL.fieldOf("relative_mode").forGetter(GyroConfig::relativeMode),
            Codec.BOOL.fieldOf("invert_pitch").forGetter(GyroConfig::invertPitch),
            Codec.BOOL.fieldOf("invert_yaw").forGetter(GyroConfig::invertYaw),
            GyroButtonMode.CODEC.fieldOf("button_mode").forGetter(GyroConfig::buttonMode),
            GyroYawMode.CODEC.fieldOf("yaw_mode").forGetter(GyroConfig::yawMode)
    ).apply(instance, GyroConfig::new));

    public record CalibrationConfig(
            boolean calibrated,
            boolean delayedCalibration,
            GyroStateC calibration
    ) {
        public static final Codec<CalibrationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("calibrated").forGetter(CalibrationConfig::calibrated),
                Codec.BOOL.fieldOf("delayed_calibration").forGetter(CalibrationConfig::delayedCalibration),
                GyroStateC.CODEC_MUTABLE.optionalFieldOf("calibration", GyroStateC.ZERO).forGetter(CalibrationConfig::calibration)
        ).apply(instance, CalibrationConfig::new));
    }
}
