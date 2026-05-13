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
    private final BooleanSupplier toggleCondition;
    private boolean pressed;
    private boolean waitingForRelease;
    private boolean lastToggleMode;

    public KeyMappingEmulationOutput(ControllerEntity controller, InputBinding binding, KeyMapping keyMapping, BooleanSupplier toggleCondition) {
        this.controller = controller;
        this.stateAccess = binding.createStateAccess(1, state -> push());
        this.keyMapping = keyMapping;
        this.toggleCondition = toggleCondition;

        if (toggleCondition != null) {
            lastToggleMode = toggleCondition.getAsBoolean();
            ((KeyMappingHandle) keyMapping).controlify$addToggleCondition(controller, toggleCondition);
        }
    }

    @Override
    public boolean get() {
        throw new IllegalStateException("Should never retrieve output of key mapping emulation!");
    }

    private void push() {
        boolean inputPressed = stateAccess.digital(0);
        boolean toggleMode = toggleCondition != null && toggleCondition.getAsBoolean();

        if (pressed && toggleMode != lastToggleMode) {
            ((KeyMappingHandle) keyMapping).controlify$forceSetPressed(false);
            pressed = false;
            waitingForRelease = inputPressed;
        }
        lastToggleMode = toggleMode;

        boolean suppressed = stateAccess.isSuppressed()
                || ControlifyApi.get().getCurrentController().orElse(null) != controller
                || !ControlifyApi.get().currentInputMode().isController()
                || Minecraft.getInstance().screen != null;

        if (!inputPressed) {
            waitingForRelease = false;
        } else if (suppressed) {
            waitingForRelease = true;
        }

        boolean nextPressed = inputPressed && !suppressed && !waitingForRelease;
        if (pressed == nextPressed)
            return;

        ((KeyMappingHandle) keyMapping).controlify$setPressed(nextPressed);
        pressed = nextPressed;
    }
}
