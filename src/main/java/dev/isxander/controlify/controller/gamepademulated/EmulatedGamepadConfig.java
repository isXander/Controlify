package dev.isxander.controlify.controller.gamepademulated;

import dev.isxander.controlify.controller.gamepad.GamepadConfig;
import dev.isxander.controlify.controller.gamepademulated.mapping.UserGamepadMapping;

public class EmulatedGamepadConfig extends GamepadConfig {
    public UserGamepadMapping mapping = UserGamepadMapping.NO_MAPPING;
}
