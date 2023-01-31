package dev.isxander.controlify.controller;

import org.lwjgl.glfw.GLFW;

public record AxesState(
        float leftStickX, float leftStickY,
        float rightStickX, float rightStickY,
        float leftTrigger, float rightTrigger
) {
    public static AxesState EMPTY = new AxesState(0, 0, 0, 0, 0, 0);

    public AxesState leftJoystickDeadZone(float deadZoneX, float deadZoneY) {
        return new AxesState(
                Math.abs(leftStickX) < deadZoneX ? 0 : leftStickX,
                Math.abs(leftStickY) < deadZoneY ? 0 : leftStickY,
                rightStickX, rightStickY, leftTrigger, rightTrigger
        );
    }

    public AxesState rightJoystickDeadZone(float deadZoneX, float deadZoneY) {
        return new AxesState(
                leftStickX, leftStickY,
                Math.abs(rightStickX) < deadZoneX ? 0 : rightStickX,
                Math.abs(rightStickY) < deadZoneY ? 0 : rightStickY,
                leftTrigger, rightTrigger
        );
    }

    public AxesState leftTriggerDeadZone(float deadZone) {
        return new AxesState(
                leftStickX, leftStickY, rightStickX, rightStickY,
                Math.abs(leftTrigger) < deadZone ? 0 : leftTrigger,
                rightTrigger
        );
    }

    public AxesState rightTriggerDeadZone(float deadZone) {
        return new AxesState(
                leftStickX, leftStickY, rightStickX, rightStickY,
                leftTrigger,
                Math.abs(rightTrigger) < deadZone ? 0 : rightTrigger
        );
    }

    public static AxesState fromController(Controller controller) {
        if (controller == null || !controller.connected())
            return EMPTY;

        var state = controller.getGamepadState();
        var axes = state.axes();

        float leftX = axes.get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
        float leftY = axes.get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
        float rightX = axes.get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
        float rightY = axes.get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
        float leftTrigger = (axes.get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) + 1f) / 2f;
        float rightTrigger = (axes.get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) + 1f) / 2f;

        return new AxesState(leftX, leftY, rightX, rightY, leftTrigger, rightTrigger);
    }
}
