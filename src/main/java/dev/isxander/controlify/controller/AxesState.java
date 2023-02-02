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
                deadzone(leftStickX, deadZoneX),
                deadzone(leftStickY, deadZoneY),
                rightStickX, rightStickY, leftTrigger, rightTrigger
        );
    }

    public AxesState rightJoystickDeadZone(float deadZoneX, float deadZoneY) {
        return new AxesState(
                leftStickX, leftStickY,
                deadzone(rightStickX, deadZoneX),
                deadzone(rightStickY, deadZoneY),
                leftTrigger, rightTrigger
        );
    }

    public AxesState leftTriggerDeadZone(float deadZone) {
        return new AxesState(
                leftStickX, leftStickY, rightStickX, rightStickY,
                deadzone(leftTrigger, deadZone),
                rightTrigger
        );
    }

    public AxesState rightTriggerDeadZone(float deadZone) {
        return new AxesState(
                leftStickX, leftStickY, rightStickX, rightStickY,
                leftTrigger,
                deadzone(rightTrigger, deadZone)
        );
    }

    private float deadzone(float value, float deadzone) {
        return (value - Math.copySign(Math.min(deadzone, Math.abs(value)), value)) / (1 - deadzone);
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
