package dev.isxander.controlify.controller.composable.impl;

import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controller.composable.ComposableControllerStateProvider;
import dev.isxander.controlify.controller.composable.ControllerStateModifier;
import dev.isxander.controlify.controller.composable.ModifiableControllerState;
import dev.isxander.controlify.driver.InputDriver;
import dev.isxander.controlify.utils.CUtil;

public class ComposableControllerStateProviderImpl implements ComposableControllerStateProvider {
    private final InputDriver driver;
    private final ControllerStateModifier modifier;
    private boolean warned;

    private ComposableControllerState
            stateNow = ComposableControllerState.EMPTY,
            stateThen = ComposableControllerState.EMPTY;

    public ComposableControllerStateProviderImpl(InputDriver driver, ControllerStateModifier modifier) {
        this.driver = driver;
        this.modifier = modifier;
    }

    @Override
    public ComposableControllerState stateNow() {
        return stateNow;
    }

    @Override
    public ComposableControllerState stateThen() {
        return stateThen;
    }

    @Override
    public int buttonCount() {
        return driver.numButtons();
    }

    @Override
    public int axisCount() {
        return driver.numAxes();
    }

    @Override
    public int hatCount() {
        return driver.numHats();
    }

    @Override
    public boolean supportsGyro() {
        return driver.isGyroSupported();
    }

    @Override
    public void updateState(ControllerConfig config) {
        stateThen = stateNow;
        stateNow = driver.getInputState();

        if (stateNow instanceof ModifiableControllerState modifiableState) {
            modifier.modifyState(modifiableState, config);
        } else if (!warned) {
            CUtil.LOGGER.error("The controller state provider is not modifiable! Input modifiers will not be applied.");
            warned = true;
        }
    }

    @Override
    public void clearState() {
        stateNow = ComposableControllerState.EMPTY;
        stateThen = ComposableControllerState.EMPTY;
    }

    @Override
    public void close() {
        driver.close();
    }
}
