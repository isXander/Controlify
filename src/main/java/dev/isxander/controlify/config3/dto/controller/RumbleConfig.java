package dev.isxander.controlify.config3.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record RumbleConfig(
        boolean enabled,
        Map<Identifier, Float> vibrationStrengths
) {
    public static final Codec<RumbleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(RumbleConfig::enabled),
            Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).optionalFieldOf("strengths", Map.of()).forGetter(RumbleConfig::vibrationStrengths)
    ).apply(instance, RumbleConfig::new));
}
