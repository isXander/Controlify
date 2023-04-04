package dev.isxander.controlify.rumble;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public final class BasicRumbleEffect implements RumbleEffect {
    private final RumbleState[] keyframes;
    private int tick = 0;
    private boolean finished;
    private int priority = 0;

    public BasicRumbleEffect(RumbleState[] keyframes) {
        this.keyframes = keyframes;
    }

    @Override
    public RumbleState nextState() {
        tick++;
        if (tick >= keyframes.length)
            finished = true;

        return keyframes[tick - 1];
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public int priority() {
        return priority;
    }

    public BasicRumbleEffect prioritised(int priority) {
        this.priority = priority;
        return this;
    }

    public RumbleState[] states() {
        return keyframes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BasicRumbleEffect) obj;
        return Arrays.equals(this.states(), that.states())
                && this.priority() == that.priority();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(this.states()), this.priority());
    }

    @Override
    public String toString() {
        return "RumbleEffect[" +
                "states=" + Arrays.toString(this.states()) + ',' +
                "priority=" + this.priority() + ']';
    }

    /**
     * Creates a rumble effect where the state is determined by the tick.
     *
     * @param stateFunction the function that takes a tick and returns the state for that tick.
     * @param durationTicks how many ticks the effect should last for.
     */
    public static BasicRumbleEffect byTick(Function<Integer, RumbleState> stateFunction, int durationTicks) {
        RumbleState[] states = new RumbleState[durationTicks];
        for (int i = 0; i < durationTicks; i++) {
            states[i] = stateFunction.apply(i);
        }
        return new BasicRumbleEffect(states);
    }

    /**
     * Creates a rumble effect from a function that takes a time value from 0, start, to 1, end, and returns that tick.
     *
     * @param stateFunction the function that takes the time value and returns the state for that tick.
     * @param durationTicks how many ticks the effect should last for.
     */
    public static BasicRumbleEffect byTime(Function<Float, RumbleState> stateFunction, int durationTicks) {
        return BasicRumbleEffect.byTick(tick -> stateFunction.apply((float) tick / (float) durationTicks), durationTicks);
    }

    /**
     * Creates a rumble effect that has a constant state.
     *
     * @param strong        the strong motor magnitude.
     * @param weak          the weak motor magnitude
     * @param durationTicks how many ticks the effect should last for.
     */
    public static BasicRumbleEffect constant(float strong, float weak, int durationTicks) {
        return BasicRumbleEffect.byTick(tick -> new RumbleState(strong, weak), durationTicks);
    }

    public static BasicRumbleEffect empty(int durationTicks) {
        return BasicRumbleEffect.byTick(tick -> new RumbleState(0f, 0f), durationTicks);
    }

    public static BasicRumbleEffect join(BasicRumbleEffect... effects) {
        int totalTicks = 0;
        for (BasicRumbleEffect effect : effects) {
            totalTicks += effect.states().length;
        }

        RumbleState[] states = new RumbleState[totalTicks];
        int currentTick = 0;
        for (BasicRumbleEffect effect : effects) {
            for (RumbleState state : effect.states()) {
                states[currentTick] = state;
                currentTick++;
            }
        }

        return new BasicRumbleEffect(states);
    }

    public BasicRumbleEffect join(BasicRumbleEffect other) {
        return BasicRumbleEffect.join(this, other);
    }

    public BasicRumbleEffect repeat(int count) {
        Validate.isTrue(count > 0, "count must be greater than 0");

        if (count == 1) return this;

        BasicRumbleEffect effect = this;
        for (int i = 0; i < count - 1; i++) {
            effect = BasicRumbleEffect.join(effect, this);
        }
        return effect;
    }

    public ContinuousRumbleEffect continuous() {
        int lastIndex = this.states().length - 1;
        return new ContinuousRumbleEffect(index -> this.states()[index % lastIndex], this.priority());
    }
}
