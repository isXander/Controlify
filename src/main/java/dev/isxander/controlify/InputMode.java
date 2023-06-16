package dev.isxander.controlify;

public enum InputMode {
    KEYBOARD_MOUSE,
    CONTROLLER,
    MIXED;

    public boolean isKeyboardMouse() {
        return this != CONTROLLER;
    }

    public boolean isController() {
        return this != KEYBOARD_MOUSE;
    }
}
