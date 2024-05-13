package dev.isxander.controlify.bindings.v2;

public class SimpleDigitalOutput implements DigitalOutput {
    private final StateAccess stateAccess;
    private final int history;

    public SimpleDigitalOutput(InputBinding binding, int history) {
        this.stateAccess = binding.createStateAccess(history + 1);
        this.history = history;
    }

    @Override
    public boolean get() {
        return stateAccess.digital(history);
    }
}
