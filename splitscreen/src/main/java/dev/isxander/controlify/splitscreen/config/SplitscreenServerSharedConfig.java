package dev.isxander.controlify.splitscreen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SplitscreenServerSharedConfig(AudioMethod audioMethod) {

    public static final Codec<SplitscreenServerSharedConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AudioMethod.CODEC.fieldOf("audio_method").forGetter(cfg -> cfg.audioMethod)
            ).apply(instance, SplitscreenServerSharedConfig::new)
    );

}
