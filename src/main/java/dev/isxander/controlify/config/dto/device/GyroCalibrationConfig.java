package dev.isxander.controlify.config.dto.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.config.dto.profile.GyroConfig;
import dev.isxander.controlify.controller.gyro.GyroStateC;

public record GyroCalibrationConfig(
        GyroStateC offset
) {
    public static final Codec<GyroCalibrationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GyroStateC.CODEC_MUTABLE.optionalFieldOf("offset", GyroStateC.ZERO).forGetter(GyroCalibrationConfig::offset)
    ).apply(instance, GyroCalibrationConfig::new));
}
