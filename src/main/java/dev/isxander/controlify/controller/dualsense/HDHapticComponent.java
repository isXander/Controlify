package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

public class HDHapticComponent implements ECSComponent, ConfigHolder<HDHapticComponent.Config> {
    public static final ResourceLocation ID = Controlify.id("hd_haptics");

    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);
    private final Event<PlayHapticEvent> playHapticEvent;
    private final RandomSource randomSource;

    public HDHapticComponent() {
        this.playHapticEvent = EventFactory.createArrayBacked(PlayHapticEvent.class, listeners -> buffer -> {
            for (PlayHapticEvent hapticBuffer : listeners) {
                hapticBuffer.play(buffer);
            }
        });
        this.randomSource = RandomSource.create();
    }

    public void playHaptic(ResourceLocation haptic) {
        if (!confObj().enabled) return;

        HapticBufferLibrary.INSTANCE.getHaptic(haptic)
                .thenAccept(playHapticEvent.invoker()::play);
    }

    public void playHaptic(SoundEvent sound) {
        ResourceLocation location = Minecraft.getInstance().getSoundManager()
                .getSoundEvent(sound.getLocation())
                .getSound(randomSource).getLocation();
        this.playHaptic(new ResourceLocation(location.getNamespace(), "sounds/" + location.getPath() + ".ogg"));
    }

    public Event<PlayHapticEvent> getPlayHapticEvent() {
        return playHapticEvent;
    }

    @Override
    public IConfig<Config> config() {
        return config;
    }

    public static class Config implements ConfigClass {
        public boolean enabled = true;
    }

    public interface PlayHapticEvent {
        void play(HapticBufferLibrary.HapticBuffer buffer);
    }
}
