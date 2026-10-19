package dev.isxander.controlify.impl.action;

/// Accepts action state.
public interface Accumulator {
	default void firePulse() {}

	default void toggleLatch() {}
	default void setLatch(boolean active) {}

	default void setContinuous(float value) {}
}
