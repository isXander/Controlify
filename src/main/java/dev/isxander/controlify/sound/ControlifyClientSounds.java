package dev.isxander.controlify.sound;

import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class ControlifyClientSounds {
    public static final Supplier<SoundEvent> SCREEN_FOCUS_CHANGE = register("controlify.ui.focus");

    private static Supplier<SoundEvent> register(String id) {
        ResourceLocation location = CUtil.rl(id);

        // don't actually register the event, so it isn't synced with the server
        SoundEvent sound = SoundEvent.createVariableRangeEvent(location);
        return () -> sound;
    }

    public static void init() {
        // calling init now calls <clinit> which is where the sounds are registered
    }
}
