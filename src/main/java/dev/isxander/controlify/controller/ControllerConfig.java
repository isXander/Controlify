package dev.isxander.controlify.controller;

import dev.isxander.controlify.config.ControlifyConfig;

public class ControllerConfig {
    public static final ControllerConfig DEFAULT = new ControllerConfig();

    public float leftStickDeadzone = 0.2f;
    public float rightStickDeadzone = 0.2f;

    // not sure if triggers need deadzones
    public float leftTriggerDeadzone = 0.0f;
    public float rightTriggerDeadzone = 0.0f;

    public float leftTriggerActivationThreshold = 0.5f;
    public float rightTriggerActivationThreshold = 0.5f;

    public String customName = null;

    public void notifyChanged() {
        ControlifyConfig.save();
    }

    public void overwrite(ControllerConfig from) {
        this.leftStickDeadzone = from.leftStickDeadzone;
        this.rightStickDeadzone = from.rightStickDeadzone;
        this.leftTriggerDeadzone = from.leftTriggerDeadzone;
        this.rightTriggerDeadzone = from.rightTriggerDeadzone;
        this.leftTriggerActivationThreshold = from.leftTriggerActivationThreshold;
        this.rightTriggerActivationThreshold = from.rightTriggerActivationThreshold;
        this.customName = from.customName;
    }
}
