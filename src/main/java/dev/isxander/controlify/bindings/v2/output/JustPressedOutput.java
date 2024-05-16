package dev.isxander.controlify.bindings.v2.output;

import dev.isxander.controlify.bindings.v2.InputBinding;
import dev.isxander.controlify.bindings.v2.StateAccess;

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
