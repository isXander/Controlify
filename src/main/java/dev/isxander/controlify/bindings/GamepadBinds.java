package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;

import java.util.Optional;
import java.util.function.Function;

public enum GamepadBinds {
    A_BUTTON(state -> state.gamepadButtons().a(), "a_button"),
    B_BUTTON(state -> state.gamepadButtons().b(), "b_button"),
    X_BUTTON(state -> state.gamepadButtons().x(), "x_button"),
    Y_BUTTON(state -> state.gamepadButtons().y(), "y_button"),
    LEFT_BUMPER(state -> state.gamepadButtons().leftBumper(), "left_bumper"),
    RIGHT_BUMPER(state -> state.gamepadButtons().rightBumper(), "right_bumper"),
    LEFT_STICK_PRESS(state -> state.gamepadButtons().leftStick(), "left_stick_press"),
    RIGHT_STICK_PRESS(state -> state.gamepadButtons().rightStick(), "right_stick_press"),
    START(state -> state.gamepadButtons().start(), "start"),
    BACK(state -> state.gamepadButtons().back(), "back"),
    GUIDE(state -> state.gamepadButtons().guide(), "guide"), // the middle button
    DPAD_UP(state -> state.gamepadButtons().dpadUp(), "dpad_up"),
    DPAD_DOWN(state -> state.gamepadButtons().dpadDown(), "dpad_down"),
    DPAD_LEFT(state -> state.gamepadButtons().dpadLeft(), "dpad_left"),
    DPAD_RIGHT(state -> state.gamepadButtons().dpadRight(), "dpad_right"),
    LEFT_TRIGGER(state -> state.gamepadAxes().leftTrigger(), "left_trigger", true),
    RIGHT_TRIGGER(state -> state.gamepadAxes().rightTrigger(), "right_trigger", true),
    LEFT_STICK_FORWARD(state -> -Math.min(0, state.gamepadAxes().leftStickY()), "left_stick_up", true),
    LEFT_STICK_BACKWARD(state -> Math.max(0, state.gamepadAxes().leftStickY()), "left_stick_down", true),
    LEFT_STICK_LEFT(state -> -Math.min(0, state.gamepadAxes().leftStickX()), "left_stick_left", true),
    LEFT_STICK_RIGHT(state -> Math.max(0, state.gamepadAxes().leftStickX()), "left_stick_right", true),
    RIGHT_STICK_FORWARD(state -> -Math.min(0, state.gamepadAxes().rightStickY()), "right_stick_up", true),
    RIGHT_STICK_BACKWARD(state -> Math.max(0, state.gamepadAxes().rightStickY()), "right_stick_down", true),
    RIGHT_STICK_LEFT(state -> -Math.min(0, state.gamepadAxes().rightStickX()), "right_stick_left", true),
    RIGHT_STICK_RIGHT(state -> Math.max(0, state.gamepadAxes().rightStickX()), "right_stick_right", true);

    public static final String BIND_ID = "gamepad";

    private final Function<GamepadState, Float> state;
    private final String identifier;

    GamepadBinds(Function<GamepadState, Float> state, String identifier, boolean jvmIsBad) {
        this.state = state;
        this.identifier = identifier;
    }

    GamepadBinds(Function<GamepadState, Boolean> state, String identifier) {
        this(state1 -> state.apply(state1) ? 1f : 0f, identifier, true);
    }

    public GamepadBind forGamepad(GamepadController gamepad) {
        return new GamepadBind(state, identifier, gamepad);
    }

    public static Optional<GamepadBinds> fromJson(JsonObject object) {
        String name = object.get("bind").getAsString();
        for (GamepadBinds bind : values()) {
            if (bind.identifier.equals(name)) return Optional.of(bind);
        }
        return Optional.empty();
    }
}
