package dev.isxander.controlify.input.action;

public enum ChannelKind {
    CONTINUOUS,
    PULSE,
    LATCH;

    public boolean isContinuous() {
        return this == CONTINUOUS;
    }

    public boolean isPulse() {
        return this == PULSE;
    }

    public boolean isLatch() {
        return this == LATCH;
    }
}
