package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.StateAccess;

public class JustPressedOutput implements DigitalOutput {
    private final StateAccess stateAccess;

    public JustPressedOutput(InputBinding binding) {
        this.stateAccess = binding.createStateAccess(2);
    }

    @Override
    public boolean get() {
        if (stateAccess.isSuppressed())
            return false;

        return stateAccess.digital(0) && !stateAccess.digital(1);
    }
}
