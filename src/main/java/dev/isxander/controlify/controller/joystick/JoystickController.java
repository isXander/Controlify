package dev.isxander.controlify.controller.joystick;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.joystick.JoystickConfig;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;

public interface JoystickController<T extends JoystickConfig> extends Controller<JoystickState, T> {
    JoystickMapping mapping();

    int axisCount();
    int buttonCount();
    int hatCount();

    @Override
    default boolean canBeUsed() {
        return !(mapping() instanceof UnmappedJoystickMapping);
    }
}
