package dev.isxander.controlify.rumble;

public record RumbleState(float strong, float weak) {
    public static final RumbleState NONE = new RumbleState(0.0F, 0.0F);

    public boolean isZero() {
        return strong == 0.0F && weak == 0.0F;
    }

    public RumbleState mul(float multiplier) {
        return new RumbleState(strong * multiplier, weak * multiplier);
    }
}
