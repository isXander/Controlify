package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.ControllerState;

@FunctionalInterface
public interface Bind {
    boolean state(ControllerState controllerState);

    Bind A_BUTTON = state -> state.buttons().a();
    Bind B_BUTTON = state -> state.buttons().b();
    Bind X_BUTTON = state -> state.buttons().x();
    Bind Y_BUTTON = state -> state.buttons().y();
    Bind LEFT_BUMPER = state -> state.buttons().leftBumper();
    Bind RIGHT_BUMPER = state -> state.buttons().rightBumper();
    Bind LEFT_STICK = state -> state.buttons().leftStick();
    Bind RIGHT_STICK = state -> state.buttons().rightStick();
    Bind START = state -> state.buttons().start();
    Bind BACK = state -> state.buttons().back();
    Bind LEFT_TRIGGER = leftTrigger(0.5f);
    Bind RIGHT_TRIGGER = rightTrigger(0.5f);

    Bind[] ALL = {
            A_BUTTON, B_BUTTON, X_BUTTON, Y_BUTTON,
            LEFT_BUMPER, RIGHT_BUMPER,
            LEFT_STICK, RIGHT_STICK,
            START, BACK,
            LEFT_TRIGGER, RIGHT_TRIGGER
    };

    static Bind leftTrigger(float threshold) {
        return state -> state.axes().leftTrigger() > threshold;
    }

    static Bind rightTrigger(float threshold) {
        return state -> state.axes().rightTrigger() > threshold;
    }
}
