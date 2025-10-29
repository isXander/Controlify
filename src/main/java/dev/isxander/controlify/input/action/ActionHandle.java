package dev.isxander.controlify.input.action;

/**
 * A handle to an action's current state.
 * This allows verifiable and type safe access to the action's state,
 * since an action can only have one channel type.
 */
public sealed interface ActionHandle {
    Action action();

    record Latch(Action action) implements ActionHandle, Channel.Latch {
        @Override
        public boolean isLatchActive() {
            return this.action().latchChannel().isLatchActive();
        }
    }

    record Pulse(Action action) implements ActionHandle, Channel.Pulse {
        @Override
        public boolean consumePulse() {
            return this.action().pulseChannel().consumePulse();
        }
    }

    record Continuous(Action action) implements ActionHandle, Channel.Continuous {
        @Override
        public float getContinuous() {
            return this.action().continuousChannel().getContinuous();
        }
    }
}
