package dev.isxander.controlify.haptics.rumble.effects;

import dev.isxander.controlify.haptics.rumble.DynamicRumbleEffect;
import org.jetbrains.annotations.Nullable;

public interface UseItemEffectHolder {
    @Nullable DynamicRumbleEffect controlify$getUseItemEffect();
}
