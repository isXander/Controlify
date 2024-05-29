package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

import java.util.function.Consumer;

public class HDHapticComponent implements ECSComponent, ConfigHolder<HDHapticComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("hd_haptics");

    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);
    private Consumer<HapticBufferLibrary.HapticBuffer> playHapticConsumer;
    private final RandomSource randomSource;

    public HDHapticComponent() {
        this.randomSource = RandomSource.create();
    }

    public void playHaptic(ResourceLocation haptic) {
        if (!confObj().enabled || playHapticConsumer == null) return;

        HapticBufferLibrary.INSTANCE.getHaptic(haptic)
                .thenAccept(playHapticConsumer);
    }

    public void playHaptic(SoundEvent sound) {
        ResourceLocation location = Minecraft.getInstance().getSoundManager()
                .getSoundEvent(sound.getLocation())
                .getSound(randomSource).getLocation();
        this.playHaptic(CUtil.rl(location.getNamespace(), "sounds/" + location.getPath() + ".ogg"));
    }

    public void acceptPlayHaptic(Consumer<HapticBufferLibrary.HapticBuffer> consumer) {
        this.playHapticConsumer = consumer;
    }

    @Override
    public IConfig<Config> config() {
        return config;
    }

    public static class Config implements ConfigClass {
        public boolean enabled = true;
    }
}
