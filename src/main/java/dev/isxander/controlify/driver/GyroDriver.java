package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.gamepad.GamepadState;

public interface GyroDriver extends Driver {
    GamepadState.GyroState getGyroState();

    boolean isGyroSupported();

    String getGyroDetails();

    GyroDriver UNSUPPORTED = new GyroDriver() {
        @Override
        public void update() {
        }

        @Override
        public GamepadState.GyroState getGyroState() {
            return GamepadState.GyroState.ORIGIN;
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
