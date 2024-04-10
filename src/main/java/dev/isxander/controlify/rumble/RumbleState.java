package dev.isxander.controlify.rumble;

import com.mojang.serialization.Codec;

public record RumbleState(float strong, float weak) {
    public static final Codec<RumbleState> CODEC = Codec.INT
            .xmap(RumbleState::unpackFromInt, RumbleState::packToInt);

    public static final RumbleState NONE = new RumbleState(0.0F, 0.0F);

    public boolean isZero() {
        return strong == 0.0F && weak == 0.0F;
    }

    public RumbleState mul(float multiplier) {
        return new RumbleState(strong * multiplier, weak * multiplier);
    }

    public static RumbleState unpackFromInt(int packed) {
        float strong = (short)(packed >> 16) / 32767.0F;
        float weak = (short)packed / 32767.0F;
        return new RumbleState(strong, weak);
    }

    public static int packToInt(RumbleState state) {
        int high = (int)(state.strong() * 32767.0F);
        int low = (int)(state.weak() * 32767.0F);
        return (high << 16) | (low & 0xFFFF);
    }
}
