package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.StateAccess;

public class SimpleDigitalOutput implements DigitalOutput {
    private final StateAccess stateAccess;
    private final int history;

    public SimpleDigitalOutput(InputBinding binding, int history) {
        this.stateAccess = binding.createStateAccess(history + 1);
        this.history = history;
    }

    @Override
    public boolean get() {
        if (stateAccess.isSuppressed())
            return false;

        return stateAccess.digital(history);
    }
}
