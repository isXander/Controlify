package dev.isxander.controlify.rumble;

public interface RumbleCapable {
    boolean setRumble(float strongMagnitude, float weakMagnitude);

    boolean supportsRumble();

    RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source);
}
