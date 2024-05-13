package dev.isxander.controlify.bindings.v2;

public class JustReleasedOutput implements DigitalOutput {
    private final StateAccess stateAccess;

    public JustReleasedOutput(InputBinding binding) {
        this.stateAccess = binding.createStateAccess(2);
    }

    @Override
    public boolean get() {
        return !stateAccess.digital(0) && stateAccess.digital(1);
    }
}
