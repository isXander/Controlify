package dev.isxander.controlify.mixins.feature.hdhaptics;

import com.mojang.blaze3d.audio.SoundBuffer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

@Mixin(SoundBuffer.class)
public interface SoundBufferAccessor {
    @Accessor
    @Nullable ByteBuffer getData();

    @Accessor
    AudioFormat getFormat();
}
