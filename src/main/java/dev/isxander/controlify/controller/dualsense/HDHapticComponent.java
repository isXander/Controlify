package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.config.*;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

import java.util.function.Consumer;

public class HDHapticComponent implements ComponentWithConfig<HDHapticComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("hd_haptics");
    public static final ConfigModule<Config> CONFIG_MODULE = new ConfigModule<>(ID, Config.class);

    private final ConfigInstance<Config> config;
    private Consumer<HapticBufferLibrary.HapticBuffer> playHapticConsumer;
    private final RandomSource randomSource;

    public HDHapticComponent(ControllerEntity controller) {
        this.config = new ConfigInstanceImpl<>(ID, ModuleRegistry.INSTANCE, controller);
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
    public ConfigInstance<Config> getConfigInstance() {
        return config;
    }

    public static class Config implements ConfigObject {
        public boolean enabled = true;
    }
}
