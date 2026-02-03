package dev.isxander.controlify.config.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.config.dto.device.DeviceConfig;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;

import java.util.List;
import java.util.Map;

public record ControlifyConfig(
        List<ProfileConfig> profileConfig,
        GlobalConfig globalConfig,
        Map<String, DeviceConfig> deviceConfig
) {
    public static final Codec<ControlifyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ProfileConfig.CODEC.listOf().fieldOf("profiles").forGetter(ControlifyConfig::profileConfig),
            GlobalConfig.CODEC.fieldOf("global").forGetter(ControlifyConfig::globalConfig),
            Codec.unboundedMap(Codec.STRING, DeviceConfig.CODEC).fieldOf("devices").forGetter(ControlifyConfig::deviceConfig)
    ).apply(instance, ControlifyConfig::new));
}
