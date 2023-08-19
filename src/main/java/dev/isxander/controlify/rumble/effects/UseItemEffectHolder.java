package dev.isxander.controlify.rumble.effects;

import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import org.jetbrains.annotations.Nullable;

public interface UseItemEffectHolder {
    @Nullable ContinuousRumbleEffect controlify$getUseItemEffect();
}
