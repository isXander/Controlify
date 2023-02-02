package dev.isxander.controlify.controller;

public class ControllerConfig {
    public static final ControllerConfig DEFAULT = new ControllerConfig();

    public float horizontalLookSensitivity = 1f;
    public float verticalLookSensitivity = 0.9f;

    public float leftStickDeadzone = 0.2f;
    public float rightStickDeadzone = 0.2f;

    // not sure if triggers need deadzones
    public float leftTriggerDeadzone = 0.0f;
    public float rightTriggerDeadzone = 0.0f;

    public float leftTriggerActivationThreshold = 0.5f;
    public float rightTriggerActivationThreshold = 0.5f;

    public int screenRepeatNavigationDelay = 4;

    public float virtualMouseSensitivity = 1f;

    public String customName = null;
}
