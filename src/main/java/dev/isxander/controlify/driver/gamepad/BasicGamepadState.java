package dev.isxander.controlify.driver.gamepad;

import dev.isxander.controlify.controller.gamepad.GamepadState;

public record BasicGamepadState(GamepadState.AxesState axes, GamepadState.ButtonState buttons) {
    public static final BasicGamepadState EMPTY = new BasicGamepadState(GamepadState.AxesState.EMPTY, GamepadState.ButtonState.EMPTY);
}
