package dev.isxander.controlify.rumble;

public record TriggerRumbleState(float left, float right) {
    public static final TriggerRumbleState NONE = new TriggerRumbleState(0.0F, 0.0F);

    public boolean isZero() {
        return left == 0.0F && right == 0.0F;
    }

    public TriggerRumbleState mul(float multiplier) {
        return new TriggerRumbleState(left * multiplier, right * multiplier);
    }
}
