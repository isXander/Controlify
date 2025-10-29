package dev.isxander.controlify.input.action;

public sealed interface Channel {
    non-sealed interface Continuous extends Channel {
        float getContinuous();

        default Latch asLatch(float threshold) {
            return () -> getContinuous() >= threshold;
        }

        default Latch asLatch() {
            return asLatch(0.5f);
        }
    }
    non-sealed interface Pulse extends Channel {
        boolean consumePulse();
    }
    non-sealed interface Latch extends Channel {
        boolean isLatchActive();
    }
}
