package dev.isxander.controlify.input.action;

public sealed interface Channel {
    non-sealed interface Continuous extends Channel {
        float value();
    }
    non-sealed interface Pulse extends Channel {
        boolean isPulseFiring();
    }
    non-sealed interface Latch extends Channel {
        boolean isLatchActive();
    }
}
