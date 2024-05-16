package dev.isxander.controlify.bindings.v2.output;

import dev.isxander.controlify.bindings.v2.InputBinding;
import dev.isxander.controlify.bindings.v2.StateAccess;
import net.minecraft.client.KeyMapping;

public class KeyMappingEmulationOutput implements DigitalOutput {
    private final StateAccess stateAccess;
    private final KeyMapping keyMapping;

    private boolean state;

    public KeyMappingEmulationOutput(InputBinding binding, KeyMapping keyMapping) {
        this.stateAccess = binding.createStateAccess(2, state -> push());
        this.keyMapping = keyMapping;
    }

    @Override
    public boolean get() {
        return false;
    }

    private void push() {
        boolean now = stateAccess.digital(0);
        boolean prev = stateAccess.digital(1);

        if (now && !prev) {
            keyMapping.setDown(true);
        } else if (prev && !now) {
            keyMapping.setDown(false);
        }
    }
}
