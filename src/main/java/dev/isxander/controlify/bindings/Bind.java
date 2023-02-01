package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum Bind {
    A_BUTTON(state -> state.buttons().a(), "a_button"),
    B_BUTTON(state -> state.buttons().b(), "b_button"),
    X_BUTTON(state -> state.buttons().x(), "x_button"),
    Y_BUTTON(state -> state.buttons().y(), "y_button"),
    LEFT_BUMPER(state -> state.buttons().leftBumper(), "left_bumper"),
    RIGHT_BUMPER(state -> state.buttons().rightBumper(), "right_bumper"),
    LEFT_STICK(state -> state.buttons().leftStick(), "left_stick"),
    RIGHT_STICK(state -> state.buttons().rightStick(), "right_stick"),
    START(state -> state.buttons().start(), "start"),
    BACK(state -> state.buttons().back(), "back"),
    GUIDE(state -> state.buttons().guide(), "guide"), // the middle button
    DPAD_UP(state -> state.buttons().dpadUp(), "dpad_up"),
    DPAD_DOWN(state -> state.buttons().dpadDown(), "dpad_down"),
    DPAD_LEFT(state -> state.buttons().dpadLeft(), "dpad_left"),
    DPAD_RIGHT(state -> state.buttons().dpadRight(), "dpad_right"),
    LEFT_TRIGGER((state, controller) -> state.axes().leftTrigger() >= controller.config().leftTriggerActivationThreshold, "left_trigger"),
    RIGHT_TRIGGER((state, controller) -> state.axes().rightTrigger() >= controller.config().rightTriggerActivationThreshold, "right_trigger");

    private final BiFunction<ControllerState, Controller, Boolean> state;
    private final String identifier;
    private final ResourceLocation textureLocation;

    Bind(BiFunction<ControllerState, Controller, Boolean> state, String identifier) {
        this.state = state;
        this.identifier = identifier;
        this.textureLocation = new ResourceLocation("controlify", "textures/gui/buttons/" + identifier + ".png");
    }

    Bind(Function<ControllerState, Boolean> state, String identifier) {
        this((state1, controller) -> state.apply(state1), identifier);
    }

    public boolean state(ControllerState controllerState, Controller controller) {
        return state.apply(controllerState, controller);
    }

    public String identifier() {
        return identifier;
    }

    public ResourceLocation textureLocation() {
        return textureLocation;
    }

    public static Bind fromIdentifier(String identifier) {
        for (Bind bind : values()) {
            if (bind.identifier.equals(identifier)) return bind;
        }
        return null;
    }
}
