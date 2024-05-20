package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.KeyMappingHandle;
import dev.isxander.controlify.bindings.StateAccess;
import net.minecraft.client.KeyMapping;

public class KeyMappingEmulationOutput implements DigitalOutput {
    private final StateAccess stateAccess;
    private final KeyMapping keyMapping;

    public KeyMappingEmulationOutput(InputBinding binding, KeyMapping keyMapping) {
        this.stateAccess = binding.createStateAccess(2, state -> push());
        this.keyMapping = keyMapping;
    }

    @Override
    public boolean get() {
        throw new IllegalStateException("Should never retrieve output of key mapping emulation!");
    }

    private void push() {
        boolean now = stateAccess.digital(0);
        boolean prev = stateAccess.digital(1);

        KeyMappingHandle handle = (KeyMappingHandle) keyMapping;
        if (now && !prev) {
            handle.controlify$setPressed(true);
        } else if (prev && !now) {
            handle.controlify$setPressed(false);
        }
    }
}
