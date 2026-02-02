package dev.isxander.controlify.config.dto.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;

import java.util.Optional;

public record DeviceConfig(
    GyroCalibrationConfig gyroCalibration,
    Optional<ControllerMapping> mapping
) {
    public static final Codec<DeviceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GyroCalibrationConfig.CODEC.fieldOf("gyro_calibration").forGetter(DeviceConfig::gyroCalibration),
            ControllerMapping.CODEC.optionalFieldOf("mapping").forGetter(DeviceConfig::mapping)
    ).apply(instance, DeviceConfig::new));
}
