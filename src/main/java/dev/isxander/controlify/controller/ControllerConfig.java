package dev.isxander.controlify.controller;

public abstract class ControllerConfig {
    public float horizontalLookSensitivity = 1f;
    public float verticalLookSensitivity = 0.9f;

    public float buttonActivationThreshold = 0.5f;

    public int screenRepeatNavigationDelay = 4;

    public float virtualMouseSensitivity = 1f;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;

    public String customName = null;

    public boolean showIngameGuide = true;
    public boolean showScreenGuide = true;

    public float chatKeyboardHeight = 0f;

    public boolean reduceAimingSensitivity = true;

    public boolean allowVibrations = true;

    public boolean calibrated = false;

    public abstract void setDeadzone(int axis, float deadzone);
    public abstract float getDeadzone(int axis);
}
