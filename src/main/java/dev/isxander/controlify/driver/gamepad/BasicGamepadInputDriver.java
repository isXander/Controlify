package dev.isxander.controlify.driver.gamepad;

import dev.isxander.controlify.driver.Driver;

public interface BasicGamepadInputDriver extends Driver {

    BasicGamepadState getBasicGamepadState();

    String getBasicGamepadDetails();

    BasicGamepadInputDriver UNSUPPORTED = new BasicGamepadInputDriver() {
        @Override
        public void update() {
        }

        @Override
        public BasicGamepadState getBasicGamepadState() {
            return BasicGamepadState.EMPTY;
        }

        @Override
        public String getBasicGamepadDetails() {
            return "Unsupported";
        }
    };
}
