package dev.isxander.controlify.controller.composable.emulatedgp;

import dev.isxander.controlify.controller.composable.gamepad.GamepadConfig;
import dev.isxander.controlify.controller.composable.emulatedgp.mapping.UserGamepadMapping;

public class EmulatedGamepadConfig extends GamepadConfig {
    public UserGamepadMapping mapping = UserGamepadMapping.NO_MAPPING;
}
