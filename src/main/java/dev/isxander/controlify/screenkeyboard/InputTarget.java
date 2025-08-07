package dev.isxander.controlify.screenkeyboard;

public interface InputTarget {
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
}
