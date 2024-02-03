package dev.isxander.controlify.controller.composable.gamepad;

import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.composable.GyroYawMode;

public class GamepadConfig extends ControllerConfig {
    public GamepadConfig() {
        super(GamepadInputs.DEADZONE_AXES);
    }
}
