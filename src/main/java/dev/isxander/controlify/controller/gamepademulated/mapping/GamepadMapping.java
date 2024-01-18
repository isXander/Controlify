package dev.isxander.controlify.controller.gamepademulated.mapping;

import dev.isxander.controlify.driver.gamepad.BasicGamepadState;
import dev.isxander.controlify.driver.joystick.BasicJoystickState;

public interface GamepadMapping {
   BasicGamepadState mapJoystick(BasicJoystickState state);
}
