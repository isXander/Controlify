package dev.isxander.controlify.controller.composable.impl;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.composable.ComposableControllerRumble;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;

public class NotRumbleCapableImpl implements ComposableControllerRumble, RumbleCapable {
    private final RumbleManager rumbleManager;

    public NotRumbleCapableImpl() {
        this.rumbleManager = new RumbleManager(this);
    }

    @Override
    public RumbleManager rumbleManager() {
        return rumbleManager;
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        return false;
    }

    @Override
    public boolean supportsRumble() {
        return false;
    }

    @Override
    public void bindController(Controller<?> controller) {

    }

    @Override
    public RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source) {
        return state;
    }
}
