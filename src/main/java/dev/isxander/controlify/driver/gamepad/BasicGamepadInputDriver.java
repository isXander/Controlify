package dev.isxander.controlify.driver.gamepad;

import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.driver.Driver;

public interface BasicGamepadInputDriver extends Driver {

    BasicGamepadState getBasicGamepadState();

    String getBasicGamepadDetails();

    record BasicGamepadState(GamepadState.AxesState axes, GamepadState.ButtonState buttons) {
        public static final BasicGamepadState EMPTY = new BasicGamepadState(GamepadState.AxesState.EMPTY, GamepadState.ButtonState.EMPTY);
    }

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
