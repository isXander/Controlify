package dev.isxander.controlify.utils;

public class ControllerUtils {
    public static float deadzone(float value, float deadzone) {
        return (value - Math.copySign(Math.min(deadzone, Math.abs(value)), value)) / (1 - deadzone);
    }
}
