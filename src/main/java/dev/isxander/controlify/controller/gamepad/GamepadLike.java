package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.controller.Controller;

public interface GamepadLike<T extends GamepadConfig> extends Controller<GamepadState, T> {
    boolean supportsGyro();

    GamepadState.GyroStateC gyroState();
}
