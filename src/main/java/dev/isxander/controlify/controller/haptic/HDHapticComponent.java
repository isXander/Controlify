package dev.isxander.controlify.controller.haptic;

import com.mojang.blaze3d.audio.SoundBuffer;
import dev.isxander.controlify.config.settings.profile.HDHapticSettings;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.mixins.feature.hdhaptics.SoundBufferAccessor;
import dev.isxander.controlify.mixins.feature.hdhaptics.SoundEngineAccessor;
import dev.isxander.controlify.mixins.feature.hdhaptics.SoundManagerAccessor;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HDHapticComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("hd_haptics");

    private Consumer<CompleteSoundData> playHapticConsumer;
    private final RandomSource randomSource;

    // the existing sound buffer library in the sound engine works on a ResourceProvider for registered sounds only
    // haptics are not sounds.
    private static final SoundBufferLibrary hapticBufferLibrary = new SoundBufferLibrary(Minecraft.getInstance().getResourceManager());
    private static final Map<Identifier, CompleteSoundData> hapticData = new HashMap<>();

    public HDHapticComponent() {
        this.randomSource = RandomSource.create();
    }

    public void playHaptic(Identifier haptic) {
        if (!settings().enabled || playHapticConsumer == null) return;

        getSoundData(haptic,  hapticBufferLibrary.getCompleteBuffer(haptic))
                .thenAccept(playHapticConsumer)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    public void playHaptic(SoundEvent sound) {
        Identifier location = Minecraft.getInstance().getSoundManager()
                .getSoundEvent(/*? if >=1.21.2 {*/ sound.location() /*?} else {*/ /*sound.getLocation() *//*?}*/)
                .getSound(randomSource).getLocation();

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        SoundEngine soundEngine = ((SoundManagerAccessor) soundManager).getSoundEngine();
        SoundBufferLibrary bufferLibrary = ((SoundEngineAccessor) soundEngine).getSoundBuffers();

        Identifier soundId = location.withPrefix("sounds/").withSuffix(".ogg");

        getSoundData(soundId, bufferLibrary.getCompleteBuffer(soundId))
                .thenAccept(playHapticConsumer)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    public void acceptPlayHaptic(Consumer<CompleteSoundData> consumer) {
        this.playHapticConsumer = consumer;
    }

    private CompletableFuture<CompleteSoundData> getSoundData(Identifier id, CompletableFuture<SoundBuffer> sound) {
        return sound
                .thenApply(soundBuffer -> hapticData.computeIfAbsent(id, key -> {
                    var accessor = (SoundBufferAccessor) soundBuffer;
                    ByteBuffer bytes = accessor.getData();
                    AudioFormat format = accessor.getFormat();

                    if (bytes == null) {
                        return null;
                    }

                    bytes.rewind();

                    byte[] audio = new byte[bytes.remaining()];

                    bytes.get(audio);

                    return new CompleteSoundData(audio, format);
                }));
    }

    public HDHapticSettings settings() {
        return this.controller().settings().hdHaptic;
    }

    public HDHapticSettings defaultSettings() {
        return this.controller().defaultSettings().hdHaptic;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
