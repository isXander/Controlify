package dev.isxander.controlify.screenkeyboard;

public interface KeyPressConsumer {
    void acceptKeyCode(int keycode, int scancode, int modifiers);

    void acceptChar(char codePoint, int modifiers);

    static KeyPressConsumer of(KeyCodeConsumer keyCodeConsumer, CharConsumer charConsumer) {
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

    interface KeyCodeConsumer {
        void accept(int keycode, int scancode, int modifiers);
    }

    interface CharConsumer {
        void accept(char codePoint, int modifiers);
    }
}
