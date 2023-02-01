package dev.isxander.controlify.controller;

import org.lwjgl.glfw.GLFW;

public record ButtonState(
        boolean a, boolean b, boolean x, boolean y,
        boolean leftBumper, boolean rightBumper,
        boolean back, boolean start, boolean guide,
        boolean dpadUp, boolean dpadDown, boolean dpadLeft, boolean dpadRight,
        boolean leftStick, boolean rightStick
) {
    public static ButtonState EMPTY = new ButtonState(
            false, false, false, false,
            false, false,
            false, false, false,
            false, false, false, false,
            false, false
    );

    public static ButtonState fromController(Controller controller) {
        if (controller == null || !controller.connected())
            return EMPTY;

        var state = controller.getGamepadState();
        var buttons = state.buttons();

        boolean a = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_A) == GLFW.GLFW_PRESS;
        boolean b = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_B) == GLFW.GLFW_PRESS;
        boolean x = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_X) == GLFW.GLFW_PRESS;
        boolean y = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_Y) == GLFW.GLFW_PRESS;
        boolean leftBumper = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW.GLFW_PRESS;
        boolean rightBumper = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW.GLFW_PRESS;
        boolean back = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == GLFW.GLFW_PRESS;
        boolean start = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS;
        boolean guide = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE) == GLFW.GLFW_PRESS;
        boolean dpadUp = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) == GLFW.GLFW_PRESS;
        boolean dpadDown = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == GLFW.GLFW_PRESS;
        boolean dpadLeft = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == GLFW.GLFW_PRESS;
        boolean dpadRight = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == GLFW.GLFW_PRESS;
        boolean leftStick = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB) == GLFW.GLFW_PRESS;
        boolean rightStick = buttons.get(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB) == GLFW.GLFW_PRESS;

        return new ButtonState(a, b, x, y, leftBumper, rightBumper, back, start, guide, dpadUp, dpadDown, dpadLeft, dpadRight, leftStick, rightStick);
    }
}
