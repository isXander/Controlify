package dev.isxander.controlify.controller.input.mapping;

import dev.isxander.controlify.controller.input.ControllerState;

public interface GamepadMapping {
   ControllerState mapJoystick(ControllerState state);
}
