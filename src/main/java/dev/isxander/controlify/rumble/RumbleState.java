package dev.isxander.controlify.rumble;

public record RumbleState(float strong, float weak) {
    public RumbleState mul(float multiplier) {
        return new RumbleState(strong * multiplier, weak * multiplier);
    }
}
