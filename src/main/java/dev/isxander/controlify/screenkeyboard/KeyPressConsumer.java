package dev.isxander.controlify.screenkeyboard;

import org.apache.commons.lang3.function.TriConsumer;

import java.util.function.BiConsumer;

public interface KeyPressConsumer {
    void acceptKeyCode(int keycode, int scancode, int modifiers);

    void acceptChar(char codePoint, int modifiers);

    static KeyPressConsumer of(TriConsumer<Integer, Integer, Integer> keyCodeConsumer, BiConsumer<Character, Integer> charConsumer) {
        return new KeyPressConsumer() {
            @Override
            public void acceptKeyCode(int keycode, int scancode, int modifiers) {
                keyCodeConsumer.accept(keycode, scancode, modifiers);
            }

            @Override
            public void acceptChar(char codePoint, int modifiers) {
                charConsumer.accept(codePoint, modifiers);
            }
        };
    }
}
