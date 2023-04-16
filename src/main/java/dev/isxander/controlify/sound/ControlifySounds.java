package dev.isxander.controlify.sound;

import dev.isxander.controlify.Controlify;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class ControlifySounds {
    public static final SoundEvent SCREEN_FOCUS_CHANGE = register("controlify.ui.focus");

    private static SoundEvent register(String id) {
        ResourceLocation location = Controlify.id(id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, location, SoundEvent.createVariableRangeEvent(location));
    }

    public static void init() {
        // calling init now calls <clinit> which is where the sounds are registered
    }
}
