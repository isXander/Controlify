package dev.isxander.controlify.controller.composable.impl;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.composable.ComposableControllerRumble;
import dev.isxander.controlify.driver.RumbleDriver;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;

public class ComposableControllerRumbleImpl implements ComposableControllerRumble, RumbleCapable {
    private final RumbleManager rumbleManager;
    private final RumbleDriver driver;
    private Controller<?> controller;

    public ComposableControllerRumbleImpl(RumbleDriver driver) {
        this.driver = driver;
        this.rumbleManager = new RumbleManager(this);
    }

    @Override
    public RumbleManager rumbleManager() {
        return rumbleManager;
    }

    @Override
    public boolean supportsRumble() {
        return driver.isRumbleSupported();
    }

    @Override
    public void bindController(Controller<?> controller) {
        this.controller = controller;
    }

    @Override
    public RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source) {
        float strengthMod = controller.config().getRumbleStrength(source);
        if (source != RumbleSource.MASTER)
            strengthMod *= controller.config().getRumbleStrength(RumbleSource.MASTER);
        return state.mul(strengthMod);
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        if (!supportsRumble()) return false;

        return driver.rumble(Math.min(strongMagnitude, 1), Math.min(weakMagnitude, 1));
    }
}
