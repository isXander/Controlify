package dev.isxander.controlify.controller.hdhaptic;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public class HDHapticComponent implements ECSComponent {
    public static final ResourceLocation ID = Controlify.id("hd_haptics");

    private final HapticBufferLibrary bufferLibrary;
    private final Queue<HapticBufferLibrary.HapticBuffer> queuedSoundsNextTick;
    private final RandomSource randomSource;

    public HDHapticComponent() {
        this.bufferLibrary = new HapticBufferLibrary(Minecraft.getInstance().getResourceManager());
        this.queuedSoundsNextTick = new ArrayDeque<>();
        this.randomSource = RandomSource.create();
    }

    public void playHaptic(ResourceLocation haptic) {
        CUtil.LOGGER.info("Playing haptic effect: {}", haptic);
        bufferLibrary.getHaptic(haptic)
                .thenAccept(queuedSoundsNextTick::add);
    }

    public void playSoundEvent(SoundEvent sound) {
        ResourceLocation location = Minecraft.getInstance().getSoundManager()
                .getSoundEvent(sound.getLocation())
                .getSound(randomSource).getLocation();
        this.playHaptic(new ResourceLocation(location.getNamespace(), "sounds/" + location.getPath() + ".ogg"));
    }

    @Nullable
    public HapticBufferLibrary.HapticBuffer pollHaptic() {
        return queuedSoundsNextTick.poll();
    }
}
