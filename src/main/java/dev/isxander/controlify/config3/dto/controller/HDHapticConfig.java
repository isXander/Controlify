package dev.isxander.controlify.config3.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HDHapticConfig(
        boolean enabled
) {
    public static final Codec<HDHapticConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(HDHapticConfig::enabled)
    ).apply(instance, HDHapticConfig::new));
}
