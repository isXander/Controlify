package dev.isxander.controlify.haptics.rumble;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Runs a fixed pre-determined pattern of rumble keyframes.
 */
public final class PatternedRumbleEffect implements RumbleEffect {
    private final RumbleState[] keyframes;
    private int tick = 0;
    private boolean finished;
    private int priority = 0;
    private BooleanSupplier earlyFinishCondition = () -> false;

    public PatternedRumbleEffect(RumbleState[] keyframes) {
        this.keyframes = keyframes;
    }

    @Override
    public void tick() {
        tick++;
        if (tick >= keyframes.length || earlyFinishCondition.getAsBoolean())
            finished = true;
    }

    @Override
    public RumbleState currentState() {
        if (tick == 0)
            throw new IllegalStateException("Effect hasn't ticked yet.");

        return keyframes[tick - 1];
    }

    @Override
    public int age() {
        return tick;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public int priority() {
        return priority;
    }

    public PatternedRumbleEffect prioritised(int priority) {
        this.priority = priority;
        return this;
    }

    public RumbleState[] states() {
        return keyframes;
    }

    public PatternedRumbleEffect earlyFinish(BooleanSupplier condition) {
        var current = earlyFinishCondition;
        this.earlyFinishCondition = () -> current.getAsBoolean() || condition.getAsBoolean();
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PatternedRumbleEffect) obj;
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

    public PatternedRumbleEffect join(PatternedRumbleEffect other) {
        return PatternedRumbleEffect.join(this, other);
    }

    public PatternedRumbleEffect repeat(int count) {
        Validate.isTrue(count > 0, "count must be greater than 0");

        if (count == 1) return this;

        PatternedRumbleEffect effect = this;
        for (int i = 0; i < count - 1; i++) {
            effect = PatternedRumbleEffect.join(effect, this);
        }
        return effect;
    }

    /**
     * Creates a rumble effect where the state is determined by the tick.
     *
     * @param stateFunction the function that takes a tick and returns the state for that tick.
     * @param durationTicks how many ticks the effect should last for.
     */
    public static PatternedRumbleEffect byTick(Function<Integer, RumbleState> stateFunction, int durationTicks) {
        RumbleState[] states = new RumbleState[durationTicks];
        for (int i = 0; i < durationTicks; i++) {
            states[i] = stateFunction.apply(i);
        }
        return new PatternedRumbleEffect(states);
    }

    /**
     * Creates a rumble effect from a function that takes a time value from 0, start, to 1, end, and returns that tick.
     *
     * @param stateFunction the function that takes the time value and returns the state for that tick.
     * @param durationTicks how many ticks the effect should last for.
     */
    public static PatternedRumbleEffect byTime(Function<Float, RumbleState> stateFunction, int durationTicks) {
        return PatternedRumbleEffect.byTick(tick -> stateFunction.apply((float) tick / (float) durationTicks), durationTicks);
    }

    /**
     * Creates a rumble effect that has a constant state.
     *
     * @param strong        the strong motor magnitude.
     * @param weak          the weak motor magnitude
     * @param durationTicks how many ticks the effect should last for.
     */
    public static PatternedRumbleEffect constant(float strong, float weak, int durationTicks) {
        return PatternedRumbleEffect.byTick(tick -> new RumbleState(strong, weak), durationTicks);
    }

    public static PatternedRumbleEffect empty(int durationTicks) {
        return PatternedRumbleEffect.byTick(tick -> new RumbleState(0f, 0f), durationTicks);
    }

    public static PatternedRumbleEffect join(PatternedRumbleEffect... effects) {
        RumbleState[] states = Arrays.stream(effects)
                .flatMap(effect -> Arrays.stream(effect.states()))
                .toArray(RumbleState[]::new);

        return new PatternedRumbleEffect(states);
    }

    public static BooleanSupplier finishOnScreenChange() {
        Screen screen = Minecraft.getInstance().screen;
        return () -> screen != Minecraft.getInstance().screen;
    }
}
