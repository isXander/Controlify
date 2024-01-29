package dev.isxander.controlify.driver.global;

public interface OnScreenKeyboardDriver {
    void openOnScreenKeyboard(int obstructionX, int obstructionY, int obstructionWidth, int obstructionHeight);

    void closeOnScreenKeyboard();

    boolean isKeyboardShown();

    String keyboardDriverDetails();

    boolean isOnScreenKeyboardSupported();

    OnScreenKeyboardDriver EMPTY = new OnScreenKeyboardDriver() {
        @Override
        public void openOnScreenKeyboard(int obstructionX, int obstructionY, int obstructionWidth, int obstructionHeight) {

        }

        @Override
        public void closeOnScreenKeyboard() {

        }

        @Override
        public boolean isKeyboardShown() {
            return false;
        }

        @Override
        public String keyboardDriverDetails() {
            return "Unsupported";
        }

        @Override
        public boolean isOnScreenKeyboardSupported() {
            return false;
        }
    };
}
