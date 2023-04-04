package dev.isxander.controlify.rumble;

import org.apache.commons.lang3.Validate;

import java.util.function.Function;

public record RumbleEffect(RumbleState[] states) {
    /**
     * Creates a rumble effect where the state is determined by the tick.
     * @param stateFunction the function that takes a tick and returns the state for that tick.
     * @param durationTicks how many ticks the effect should last for.
     */
    public static RumbleEffect byTick(Function<Integer, RumbleState> stateFunction, int durationTicks) {
        RumbleState[] states = new RumbleState[durationTicks];
        for (int i = 0; i < durationTicks; i++) {
            states[i] = stateFunction.apply(i);
        }
        return new RumbleEffect(states);
    }

    /**
     * Creates a rumble effect from a function that takes a time value from 0, start, to 1, end, and returns that tick.
     * @param stateFunction the function that takes the time value and returns the state for that tick.
     * @param durationTicks how many ticks the effect should last for.
     */
    public static RumbleEffect byTime(Function<Float, RumbleState> stateFunction, int durationTicks) {
        return RumbleEffect.byTick(tick -> stateFunction.apply((float) tick / (float) durationTicks), durationTicks);
    }

    /**
     * Creates a rumble effect that has a constant state.
     * @param strong the strong motor magnitude.
     * @param weak the weak motor magnitude
     * @param durationTicks how many ticks the effect should last for.
     */
    public static RumbleEffect constant(float strong, float weak, int durationTicks) {
        return RumbleEffect.byTick(tick -> new RumbleState(strong, weak), durationTicks);
    }

    public static RumbleEffect empty(int durationTicks) {
        return RumbleEffect.byTick(tick -> new RumbleState(0f, 0f), durationTicks);
    }

    public static RumbleEffect join(RumbleEffect... effects) {
        int totalTicks = 0;
        for (RumbleEffect effect : effects) {
            totalTicks += effect.states().length;
        }

        RumbleState[] states = new RumbleState[totalTicks];
        int currentTick = 0;
        for (RumbleEffect effect : effects) {
            for (RumbleState state : effect.states()) {
                states[currentTick] = state;
                currentTick++;
            }
        }

        return new RumbleEffect(states);
    }

    public RumbleEffect join(RumbleEffect other) {
        return RumbleEffect.join(this, other);
    }

    public RumbleEffect repeat(int count) {
        Validate.isTrue(count > 0, "count must be greater than 0");

        if (count == 1) return this;

        RumbleEffect effect = this;
        for (int i = 0; i < count - 1; i++) {
            effect = RumbleEffect.join(effect, this);
        }
        return effect;
    }
}
