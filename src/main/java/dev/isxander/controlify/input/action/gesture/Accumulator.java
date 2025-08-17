package dev.isxander.controlify.input.action.gesture;

public interface Accumulator {
    default void firePulse() {}
    default void toggleLatch() {}
    default void setLatch(boolean active) {}
    default void moveAxis(float value) {}
    default void setAxis(float value) {}
}
