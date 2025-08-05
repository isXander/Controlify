package dev.isxander.controlify.screenkeyboard;

public interface KeyboardInputConsumer {
    void acceptChar(char ch, int modifiers);

    void acceptKeyCode(int keycode, int scancode, int modifiers);
}
