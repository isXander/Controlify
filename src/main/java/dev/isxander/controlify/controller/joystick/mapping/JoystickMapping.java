package dev.isxander.controlify.controller.joystick.mapping;

import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.joystick.JoystickState;
import net.minecraft.network.chat.Component;

public interface JoystickMapping {
    Axis[] axes();
    Button[] buttons();
    Hat[] hats();

    interface Axis {
        String identifier();

        Component name();

        boolean requiresDeadzone();

        float getAxis(JoystickData data);

        boolean isAxisResting(float value);

        float restingValue();

        String getDirectionIdentifier(int axis, JoystickAxisBind.AxisDirection direction);
    }

    interface Button {
        String identifier();

        Component name();

        boolean isPressed(JoystickData data);
    }

    interface Hat {
        JoystickState.HatState getHatState(JoystickData data);

        String identifier();

        Component name();
    }

    record JoystickData(float[] axes, boolean[] buttons, JoystickState.HatState[] hats) {
    }
}
