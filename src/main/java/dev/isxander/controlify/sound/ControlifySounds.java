package dev.isxander.controlify.sound;

import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class ControlifySounds {
    public static final Supplier<SoundEvent> SCREEN_FOCUS_CHANGE = register("controlify.ui.focus");

    private static Supplier<SoundEvent> register(String id) {
        ResourceLocation location = CUtil.rl(id);

        return PlatformMainUtil.deferredRegister(BuiltInRegistries.SOUND_EVENT, location, () -> SoundEvent.createVariableRangeEvent(location));
    }

    public static void init() {
        // calling init now calls <clinit> which is where the sounds are registered
    }
}
