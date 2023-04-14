package dev.isxander.controlify.controller.joystick.mapping;

import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.render.JoystickRenderer;
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

        JoystickRenderer renderer();
    }

    interface Button {
        String identifier();

        Component name();

        boolean isPressed(JoystickData data);

        JoystickRenderer renderer();
    }

    interface Hat {
        JoystickState.HatState getHatState(JoystickData data);

        String identifier();

        Component name();

        JoystickRenderer renderer(JoystickState.HatState state);
    }

    record JoystickData(float[] axes, boolean[] buttons, JoystickState.HatState[] hats) {
    }
}
