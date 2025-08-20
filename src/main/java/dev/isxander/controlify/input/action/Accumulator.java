package dev.isxander.controlify.input.action;

public interface Accumulator {
    default void firePulse() {}
    default void toggleLatch() {}
    default void setLatch(boolean active) {}
    default void setAxis(float value) {}
}
