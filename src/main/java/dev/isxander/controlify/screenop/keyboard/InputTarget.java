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

    class Delegated implements InputTarget {
        private final InputTarget target;

        public Delegated(InputTarget target) {
            this.target = target;
        }

        @Override
        public boolean supportsCharInput() {
            return this.target.supportsCharInput();
        }

        @Override
        public boolean acceptChar(char ch, int modifiers) {
            return this.target.acceptChar(ch, modifiers);
        }

        @Override
        public boolean supportsKeyCodeInput() {
            return this.target.supportsKeyCodeInput();
        }

        @Override
        public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
            return this.target.acceptKeyCode(keycode, scancode, modifiers);
        }

        @Override
        public boolean supportsCopying() {
            return this.target.supportsCopying();
        }

        @Override
        public boolean copy() {
            return this.target.copy();
        }

        @Override
        public boolean supportsCursorMovement() {
            return this.target.supportsCursorMovement();
        }

        @Override
        public boolean moveCursor(int amount) {
            return this.target.moveCursor(amount);
        }
    }
}
