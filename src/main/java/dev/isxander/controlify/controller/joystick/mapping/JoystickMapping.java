package dev.isxander.controlify.controller.joystick.mapping;

import dev.isxander.controlify.bindings.JoystickAxisBind;
import net.minecraft.network.chat.Component;

public interface JoystickMapping {
    Axis axis(int axis);

    Button button(int button);

    Hat hat(int hat);

    interface Axis {
        String identifier();

        Component name();

        boolean requiresDeadzone();

        float modifyAxis(float value);

        boolean isAxisResting(float value);

        Component getDirectionName(int axis, JoystickAxisBind.AxisDirection direction);
    }

    interface Button {
        String identifier();

        Component name();
    }

    interface Hat {
        String identifier();

        Component name();
    }
}
