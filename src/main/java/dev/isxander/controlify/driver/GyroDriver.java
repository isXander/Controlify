package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.gamepad.GamepadState;

public interface GyroDriver extends Driver {
    GamepadState.GyroStateC getGyroState();

    boolean isGyroSupported();

    String getGyroDetails();

    GyroDriver UNSUPPORTED = new GyroDriver() {
        private final GamepadState.GyroStateC zero = new GamepadState.GyroState();

        @Override
        public void update() {
        }

        @Override
        public GamepadState.GyroStateC getGyroState() {
            return zero;
        }

        @Override
        public boolean isGyroSupported() {
            return false;
        }

        @Override
        public String getGyroDetails() {
            return "Unsupported";
        }
    };
}
