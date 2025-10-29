package dev.isxander.controlify.controller.haptic;

import com.mojang.blaze3d.audio.SoundBuffer;
import dev.isxander.controlify.config.ValueInput;
import dev.isxander.controlify.config.ValueOutput;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.mixins.feature.hdhaptics.SoundBufferAccessor;
import dev.isxander.controlify.mixins.feature.hdhaptics.SoundEngineAccessor;
import dev.isxander.controlify.mixins.feature.hdhaptics.SoundManagerAccessor;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HDHapticComponent implements ECSComponent, ConfigHolder<HDHapticComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("hd_haptics");

    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);
    private Consumer<CompleteSoundData> playHapticConsumer;
    private final RandomSource randomSource;

    // the existing sound buffer library in the sound engine works on a ResourceProvider for registered sounds only
    // haptics are not sounds.
    private static final SoundBufferLibrary hapticBufferLibrary = new SoundBufferLibrary(Minecraft.getInstance().getResourceManager());
    private static final Map<ResourceLocation, CompleteSoundData> hapticData = new HashMap<>();

    public HDHapticComponent() {
        this.randomSource = RandomSource.create();
    }

    public void playHaptic(ResourceLocation haptic) {
        if (!confObj().enabled || playHapticConsumer == null) return;

        getSoundData(haptic,  hapticBufferLibrary.getCompleteBuffer(haptic))
                .thenAccept(playHapticConsumer)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    public void playHaptic(SoundEvent sound) {
        ResourceLocation location = Minecraft.getInstance().getSoundManager()
                .getSoundEvent(/*? if >=1.21.2 {*/ sound.location() /*?} else {*/ /*sound.getLocation() *//*?}*/)
                .getSound(randomSource).getLocation();

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        SoundEngine soundEngine = ((SoundManagerAccessor) soundManager).getSoundEngine();
        SoundBufferLibrary bufferLibrary = ((SoundEngineAccessor) soundEngine).getSoundBuffers();

        ResourceLocation soundId = location.withPrefix("sounds/").withSuffix(".ogg");

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

    private CompletableFuture<CompleteSoundData> getSoundData(ResourceLocation id, CompletableFuture<SoundBuffer> sound) {
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

    @Override
    public IConfig<Config> config() {
        return config;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        public boolean enabled = true;

        @Override
        public void load(ValueInput input, ControllerEntity controller) {
            this.enabled = input.readBooleanOr("enabled", true);
        }

        @Override
        public void save(ValueOutput output, ControllerEntity controller) {
            output.putBoolean("enabled", this.enabled);
        }
    }
}
