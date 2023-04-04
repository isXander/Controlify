package dev.isxander.controlify.rumble;

import java.util.function.Function;

public class ContinuousRumbleEffect implements RumbleEffect {
    private final Function<Integer, RumbleState> stateFunction;
    private final int priority;
    private int tick;
    private boolean stopped;

    public ContinuousRumbleEffect(Function<Integer, RumbleState> stateFunction) {
        this(stateFunction, 0);
    }

    public ContinuousRumbleEffect(Function<Integer, RumbleState> stateFunction, int priority) {
        this.stateFunction = stateFunction;
        this.priority = priority;
    }

    @Override
    public RumbleState nextState() {
        tick++;
        return stateFunction.apply(tick - 1);
    }

    public void stop() {
        stopped = true;
    }

    public int currentTick() {
        return tick;
    }

    @Override
    public boolean isFinished() {
        return stopped;
    }

    @Override
    public int priority() {
        return this.priority;
    }
}
