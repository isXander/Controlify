package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.bindings.bind.BindValue;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;

import java.util.Optional;
import java.util.function.Function;

public enum GamepadBinds {
    A_BUTTON(state -> BindValue.of(state.gamepadButtons().a()), "a_button"),
    B_BUTTON(state -> BindValue.of(state.gamepadButtons().b()), "b_button"),
    X_BUTTON(state -> BindValue.of(state.gamepadButtons().x()), "x_button"),
    Y_BUTTON(state -> BindValue.of(state.gamepadButtons().y()), "y_button"),

    LEFT_BUMPER(state -> BindValue.of(state.gamepadButtons().leftBumper()), "left_bumper"),
    RIGHT_BUMPER(state -> BindValue.of(state.gamepadButtons().rightBumper()), "right_bumper"),

    LEFT_STICK_PRESS(state -> BindValue.of(state.gamepadButtons().leftStick()), "left_stick_press"),
    RIGHT_STICK_PRESS(state -> BindValue.of(state.gamepadButtons().rightStick()), "right_stick_press"),

    START(state -> BindValue.of(state.gamepadButtons().start()), "start"),
    BACK(state -> BindValue.of(state.gamepadButtons().back()), "back"),
    GUIDE(state -> BindValue.of(state.gamepadButtons().guide()), "guide"), // the middle button

    DPAD(state -> BindValue.of(state.gamepadButtons().dpadRight(), state.gamepadButtons().dpadLeft(), state.gamepadButtons().dpadDown(), state.gamepadButtons().dpadUp()), "dpad"),
    DPAD_UP(state -> BindValue.of(state.gamepadButtons().dpadUp()), "dpad_up"),
    DPAD_DOWN(state -> BindValue.of(state.gamepadButtons().dpadDown()), "dpad_down"),
    DPAD_LEFT(state -> BindValue.of(state.gamepadButtons().dpadLeft()), "dpad_left"),
    DPAD_RIGHT(state -> BindValue.of(state.gamepadButtons().dpadRight()), "dpad_right"),

    LEFT_TRIGGER(state -> BindValue.of(state.gamepadAxes().leftTrigger()), "left_trigger"),
    RIGHT_TRIGGER(state -> BindValue.of(state.gamepadAxes().rightTrigger()), "right_trigger"),

    LEFT_STICK(state -> BindValue.of(state.gamepadAxes().leftStickX(), state.gamepadAxes().leftStickY()), "left_stick"),
    LEFT_STICK_FORWARD(state -> BindValue.of(-Math.min(0, state.gamepadAxes().leftStickY())), "left_stick_up"),
    LEFT_STICK_BACKWARD(state -> BindValue.of(Math.max(0, state.gamepadAxes().leftStickY())), "left_stick_down"),
    LEFT_STICK_LEFT(state -> BindValue.of(-Math.min(0, state.gamepadAxes().leftStickX())), "left_stick_left"),
    LEFT_STICK_RIGHT(state -> BindValue.of(Math.max(0, state.gamepadAxes().leftStickX())), "left_stick_right"),

    RIGHT_STICK(state -> BindValue.of(state.gamepadAxes().rightStickX(), state.gamepadAxes().rightStickY()), "right_stick"),
    RIGHT_STICK_FORWARD(state -> BindValue.of(-Math.min(0, state.gamepadAxes().rightStickY())), "right_stick_up"),
    RIGHT_STICK_BACKWARD(state -> BindValue.of(Math.max(0, state.gamepadAxes().rightStickY())), "right_stick_down"),
    RIGHT_STICK_LEFT(state -> BindValue.of(-Math.min(0, state.gamepadAxes().rightStickX())), "right_stick_left"),
    RIGHT_STICK_RIGHT(state -> BindValue.of(Math.max(0, state.gamepadAxes().rightStickX())), "right_stick_right");

    public static final String BIND_ID = "gamepad";

    private final Function<GamepadState, BindValue> value;
    private final String identifier;

    GamepadBinds(Function<GamepadState, BindValue> value, String identifier) {
        this.value = value;
        this.identifier = identifier;
    }

    public GamepadBind forGamepad(GamepadController gamepad) {
        return new GamepadBind(value, identifier, gamepad);
    }

    public static Optional<GamepadBinds> fromJson(JsonObject object) {
        String name = object.get("bind").getAsString();
        for (GamepadBinds bind : values()) {
            if (bind.identifier.equals(name)) return Optional.of(bind);
        }
        return Optional.empty();
    }
}
