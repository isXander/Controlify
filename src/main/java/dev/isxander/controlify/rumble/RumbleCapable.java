package dev.isxander.controlify.rumble;

public interface RumbleCapable {
    boolean setRumble(float strongMagnitude, float weakMagnitude, RumbleSource source);

    boolean canRumble();
}
