package dev.isxander.controlify.rumble;

public interface RumbleEffect extends Comparable<RumbleEffect> {
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
