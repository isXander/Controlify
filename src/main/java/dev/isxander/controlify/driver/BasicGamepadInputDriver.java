package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.gamepad.GamepadState;

public interface BasicGamepadInputDriver extends Driver {

    BasicGamepadState getBasicGamepadState();

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
    };
}
