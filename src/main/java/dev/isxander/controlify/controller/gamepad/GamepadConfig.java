package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.controller.ControllerConfig;

public class GamepadConfig extends ControllerConfig {
    private float leftStickDeadzone = 0.15f;
    private float rightStickDeadzone = 0.15f;

    private transient float leftStickDeadzoneX = leftStickDeadzone;
    private transient float leftStickDeadzoneY = leftStickDeadzone;
    private transient float rightStickDeadzoneX = rightStickDeadzone;
    private transient float rightStickDeadzoneY = rightStickDeadzone;

    public float gyroLookSensitivity = 0f;
    public boolean gyroRequiresButton = true;
    public boolean flickStick = false;
    public boolean invertGyroX = false;
    public boolean invertGyroY = false;

    public BuiltinGamepadTheme theme = BuiltinGamepadTheme.DEFAULT;

    public float getLeftStickDeadzone() {
        return leftStickDeadzone;
    }

    public float getRightStickDeadzone() {
        return rightStickDeadzone;
    }

    public void setLeftStickDeadzone(float deadzone) {
        leftStickDeadzoneX = deadzone;
        leftStickDeadzoneY = deadzone;
        leftStickDeadzone = deadzone;
    }

    public void setRightStickDeadzone(float deadzone) {
        rightStickDeadzoneX = deadzone;
        rightStickDeadzoneY = deadzone;
        rightStickDeadzone = deadzone;
    }

    @Override
    public void setDeadzone(int axis, float deadzone) {
        switch (axis) {
            case 0 -> leftStickDeadzoneX = deadzone;
            case 1 -> leftStickDeadzoneY = deadzone;
            case 2 -> rightStickDeadzoneX = deadzone;
            case 3 -> rightStickDeadzoneY = deadzone;
            case 4, 5 -> {} // ignore triggers
            default -> {}
        }

        leftStickDeadzone = Math.max(leftStickDeadzoneX, leftStickDeadzoneY);
        rightStickDeadzone = Math.max(rightStickDeadzoneX, rightStickDeadzoneY);
    }

    @Override
    public float getDeadzone(int axis) {
        return switch (axis) {
            case 0, 1 -> leftStickDeadzone;
            case 2, 3 -> rightStickDeadzone;
            case 4, 5 -> 0f; // ignore triggers
            default -> throw new IllegalArgumentException("Unknown axis: " + axis);
        };
    }
}
