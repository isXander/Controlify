package dev.isxander.controlify.screenop.keyboard;

/**
 * Represents a target/consumer for keyboard input.
 * A target accepts input from the on-screen keyboard.
 * It can have various capabilities, all of which are optional to implement.
 */
public interface InputTarget {
    InputTarget EMPTY = new InputTarget(){};

    default boolean supportsCharInput() {
        return false;
    }
    default boolean acceptChar(char ch, int modifiers) {
        return false;
    }

    default boolean supportsKeyCodeInput() {
        return false;
    }
    default boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
        return false;
    }

    default boolean supportsCopying() {
        return false;
    }
    default boolean copy() {
        return false;
    }

    default boolean supportsCursorMovement() {
        return false;
    }
    default boolean moveCursor(int amount) {
        return false;
    }
}
