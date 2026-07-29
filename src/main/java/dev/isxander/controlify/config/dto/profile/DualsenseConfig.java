package dev.isxander.controlify.config.dto.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DualsenseConfig(
	boolean triggerEffects
) {
	public static final Codec<DualsenseConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.fieldOf("trigger_effects").forGetter(DualsenseConfig::triggerEffects)
	).apply(instance, DualsenseConfig::new));
}
