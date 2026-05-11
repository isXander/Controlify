package dev.isxander.controlify.bindings.output;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.KeyMappingHandle;
import dev.isxander.controlify.bindings.StateAccess;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.BooleanSupplier;

public class KeyMappingEmulationOutput implements DigitalOutput {
    private final ControllerEntity controller;
    private final StateAccess stateAccess;
    private final KeyMapping keyMapping;
    private boolean pressed;

    public KeyMappingEmulationOutput(ControllerEntity controller, InputBinding binding, KeyMapping keyMapping, BooleanSupplier toggleCondition) {
        this.controller = controller;
        this.stateAccess = binding.createStateAccess(1, state -> push());
        this.keyMapping = keyMapping;

        if (toggleCondition != null) {
            ((KeyMappingHandle) keyMapping).controlify$addToggleCondition(controller, toggleCondition);
        }
    }

    @Override
    public boolean get() {
        throw new IllegalStateException("Should never retrieve output of key mapping emulation!");
    }

    private void push() {
        boolean nextPressed = stateAccess.digital(0);

        if (stateAccess.isSuppressed()
                || ControlifyApi.get().getCurrentController().orElse(null) != controller
                || Minecraft.getInstance().screen != null) {
            nextPressed = false;
        }

        if (pressed == nextPressed)
            return;

        ((KeyMappingHandle) keyMapping).controlify$setPressed(nextPressed);
        pressed = nextPressed;
    }
}
