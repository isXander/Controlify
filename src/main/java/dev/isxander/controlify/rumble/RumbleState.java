package dev.isxander.controlify.rumble;

public record RumbleState(float strong, float weak) {
    public static final RumbleState NONE = new RumbleState(0.0F, 0.0F);

    public RumbleState mul(float multiplier) {
        return new RumbleState(strong * multiplier, weak * multiplier);
    }
}
