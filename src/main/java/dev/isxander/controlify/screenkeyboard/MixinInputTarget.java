package dev.isxander.controlify.screenkeyboard;

public interface MixinInputTarget extends InputTarget {
    default boolean controlify$supportsCharInput() {
        return false;
    }
    default boolean controlify$acceptChar(char ch, int modifiers) {
        return false;
    }

    default boolean controlify$supportsKeyCodeInput() {
        return false;
    }
    default boolean controlify$acceptKeyCode(int keycode, int scancode, int modifiers) {
        return false;
    }

    default boolean controlify$supportsCopying() {
        return false;
    }
    default boolean controlify$copy() {
        return false;
    }

    default boolean supportsCharInput() {
        return controlify$supportsCharInput();
    }
    default boolean acceptChar(char ch, int modifiers) {
        return controlify$acceptChar(ch, modifiers);
    }

    default boolean supportsKeyCodeInput() {
        return controlify$supportsKeyCodeInput();
    }
    default boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
        return controlify$acceptKeyCode(keycode, scancode, modifiers);
    }

    default boolean supportsCopying() {
        return controlify$supportsCopying();
    }
    default boolean copy() {
        return controlify$copy();
    }
}
