package dev.isxander.controlify.haptics.rumble;

public interface RumbleEffect extends Comparable<RumbleEffect> {
    /**
     * Ran once per tick,
     * it updates the {@link #currentState()}
     * and can potentially finish with {@link #isFinished()}.
     * Increments {@link #age()} by 1.
     */
    void tick();
    RumbleState currentState();

    boolean isFinished();

    int priority();
    int age();

    @Override
    default int compareTo(RumbleEffect o) {
        int priorityCompare = Integer.compare(o.priority(), this.priority());
        if (priorityCompare != 0) return priorityCompare;
        return Integer.compare(this.age(), o.age());
    }
}
