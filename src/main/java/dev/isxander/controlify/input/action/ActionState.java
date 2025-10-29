package dev.isxander.controlify.input.action;

/**
 * The mutable state of an action.
 */
final class ActionState implements Accumulator, Channel.Continuous, Channel.Pulse, Channel.Latch {
    private int pulses = 0;
    private boolean latchActive = false;
    private float axisValue = Float.NaN;

    private boolean frozen = false;

    @Override
    public float getContinuous() {
        return this.axisValue;
    }

    @Override
    public boolean isLatchActive() {
        return this.latchActive;
    }

    @Override
    public boolean consumePulse() {
        if (this.pulses > 0) {
            this.pulses--;
            return true;
        }
        return false;
    }

    @Override
    public void firePulse() {
        this.checkFrozen();
        this.pulses++;
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
    public void setContinuous(float value) {
        // Do not check frozen for continuous values; they can be updated every frame
        this.axisValue = value;
    }

    void next() {
        this.frozen = false;
    }

    void commit() {
        this.frozen = true;
    }

    Accumulator continuousOnlyAccumulator() {
        return new Accumulator() {
            @Override
            public void setContinuous(float value) {
                ActionState.this.setContinuous(value);
            }

            @Override
            public void firePulse() {
                ActionGraph.LOGGER.error("Attempted to fire pulse on continuous-only accumulator");
            }

            @Override
            public void setLatch(boolean active) {
                ActionGraph.LOGGER.error("Attempted to set latch on continuous accumulator");
            }

            @Override
            public void toggleLatch() {
                ActionGraph.LOGGER.error("Attempted to toggle latch on continuous accumulator");
            }
        };
    }

    private void checkFrozen() {
        if (this.frozen) {
            throw new IllegalStateException("Cannot modify ActionState after it has been frozen");
        }
    }
}
