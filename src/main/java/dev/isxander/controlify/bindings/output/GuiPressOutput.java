package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.StateAccess;

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
