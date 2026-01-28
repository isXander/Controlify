package dev.isxander.controlify.config3.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.config3.dto.controller.ControllerConfig;

import java.util.Optional;

public record ControlifyConfig(
        Optional<String> currentControllerUID,
        ControllerConfig controllerConfig,
        GlobalConfig globalConfig
) {
    public static final Codec<ControlifyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("current_controller").forGetter(ControlifyConfig::currentControllerUID),
            ControllerConfig.CODEC.fieldOf("controller").forGetter(ControlifyConfig::controllerConfig),
            GlobalConfig.CODEC.fieldOf("global").forGetter(ControlifyConfig::globalConfig)
    ).apply(instance, ControlifyConfig::new));
}
