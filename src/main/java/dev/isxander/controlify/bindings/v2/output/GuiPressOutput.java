package dev.isxander.controlify.bindings.v2.output;

import dev.isxander.controlify.bindings.v2.InputBinding;
import dev.isxander.controlify.bindings.v2.StateAccess;

public class GuiPressOutput implements DigitalOutput {
    private final StateAccess stateAccess;
    private boolean couldPressButton, held;

    public GuiPressOutput(InputBinding binding) {
        this.stateAccess = binding.createStateAccess(2, state -> push());
    }

    @Override
    public boolean get() {
        return couldPressButton;
    }

    private void push() {
        boolean held = stateAccess.digital(0);
        if (held) {
            if (!this.held) {
                couldPressButton = true;
            }
        } else {
            couldPressButton = false;
        }
        this.held = held;

        if (stateAccess.isSuppressed())
            couldPressButton = false;
    }

    public void onNavigate() {
        this.couldPressButton = false;
    }
}
