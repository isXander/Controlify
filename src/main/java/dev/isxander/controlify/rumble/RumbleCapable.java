package dev.isxander.controlify.rumble;

import dev.isxander.controlify.controller.ControllerConfig;

public interface RumbleCapable {
    boolean setRumble(float strongMagnitude, float weakMagnitude);

    boolean supportsRumble();

    RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source);
}
