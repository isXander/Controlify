package dev.isxander.controlify.fixes.boatfix;

import dev.isxander.controlify.mixins.feature.patches.analogueboat.BoatMixin;

/**
 * @see BoatMixin
 */
public interface AnalogBoatInput {
    void controlify$setAnalogInput(float forward, float right);
}
