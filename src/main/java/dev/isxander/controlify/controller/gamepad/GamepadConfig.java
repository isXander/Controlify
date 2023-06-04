package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.controller.ControllerConfig;

public class GamepadConfig extends ControllerConfig {
    public float leftStickDeadzoneX = 0.2f;
    public float leftStickDeadzoneY = 0.2f;
    public float rightStickDeadzoneX = 0.2f;
    public float rightStickDeadzoneY = 0.2f;

    public float gyroLookSensitivity = 0f;
    public boolean gyroRequiresButton = true;
    public boolean flickStick = false;
    public boolean invertGyroX = false;
    public boolean invertGyroY = false;

    public BuiltinGamepadTheme theme = BuiltinGamepadTheme.DEFAULT;

    @Override
    public void setDeadzone(int axis, float deadzone) {
        switch (axis) {
            case 0 -> leftStickDeadzoneX = deadzone;
            case 1 -> leftStickDeadzoneY = deadzone;
            case 2 -> rightStickDeadzoneX = deadzone;
            case 3 -> rightStickDeadzoneY = deadzone;
            default -> {}
        }
    }

    @Override
    public float getDeadzone(int axis) {
        return switch (axis) {
            case 0 -> leftStickDeadzoneX;
            case 1 -> leftStickDeadzoneY;
            case 2 -> rightStickDeadzoneX;
            case 3 -> rightStickDeadzoneY;
            default -> throw new IllegalArgumentException("Unknown axis: " + axis);
        };
    }
}
