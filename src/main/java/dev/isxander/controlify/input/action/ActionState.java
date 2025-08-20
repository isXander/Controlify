package dev.isxander.controlify.input.action;

public final class ActionState implements Accumulator, Channel.Continuous, Channel.Pulse, Channel.Latch {
    private boolean pulseFiring = false;
    private boolean latchActive = false;
    private float axisValue = Float.NaN;

    private boolean frozen = false;

    @Override
    public float value() {
        return this.axisValue;
    }

    @Override
    public boolean isLatchActive() {
        return this.latchActive;
    }

    @Override
    public boolean isPulseFiring() {
        return this.pulseFiring;
    }

    @Override
    public void firePulse() {
        this.checkFrozen();
        this.pulseFiring = true;
    }

    @Override
    public void setLatch(boolean active) {
        this.checkFrozen();
        this.latchActive = active;
    }

    @Override
    public void toggleLatch() {
        this.checkFrozen();
        this.latchActive = !this.latchActive;
    }

    @Override
    public void setAxis(float value) {
        this.checkFrozen();
        this.axisValue = value;
    }

    void next() {
        this.frozen = false;
        // intentionally don't reset latch state since it is persistent
        this.pulseFiring = false;
        this.axisValue = Float.NaN;
    }

    void commit() {
        this.frozen = true;
    }

    private void checkFrozen() {
        if (this.frozen) {
            throw new IllegalStateException("Cannot modify ActionState after it has been frozen");
        }
    }
}
