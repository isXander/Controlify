package dev.isxander.controlify.bindings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.gui.ButtonRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum Bind implements IBind {
    A_BUTTON(state -> state.buttons().a(), "a_button"),
    B_BUTTON(state -> state.buttons().b(), "b_button"),
    X_BUTTON(state -> state.buttons().x(), "x_button"),
    Y_BUTTON(state -> state.buttons().y(), "y_button"),
    LEFT_BUMPER(state -> state.buttons().leftBumper(), "left_bumper"),
    RIGHT_BUMPER(state -> state.buttons().rightBumper(), "right_bumper"),
    LEFT_STICK_PRESS(state -> state.buttons().leftStick(), "left_stick_press"),
    RIGHT_STICK_PRESS(state -> state.buttons().rightStick(), "right_stick_press"),
    START(state -> state.buttons().start(), "start"),
    BACK(state -> state.buttons().back(), "back"),
    GUIDE(state -> state.buttons().guide(), "guide"), // the middle button
    DPAD_UP(state -> state.buttons().dpadUp(), "dpad_up"),
    DPAD_DOWN(state -> state.buttons().dpadDown(), "dpad_down"),
    DPAD_LEFT(state -> state.buttons().dpadLeft(), "dpad_left"),
    DPAD_RIGHT(state -> state.buttons().dpadRight(), "dpad_right"),
    LEFT_TRIGGER((state, controller) -> state.axes().leftTrigger(), "left_trigger"),
    RIGHT_TRIGGER((state, controller) -> state.axes().rightTrigger(), "right_trigger"),
    LEFT_STICK_FORWARD((state, controller) -> -Math.min(0, state.axes().leftStickY()), "left_stick_up"),
    LEFT_STICK_BACKWARD((state, controller) -> Math.max(0, state.axes().leftStickY()), "left_stick_down"),
    LEFT_STICK_LEFT((state, controller) -> -Math.min(0, state.axes().leftStickX()), "left_stick_left"),
    LEFT_STICK_RIGHT((state, controller) -> Math.max(0, state.axes().leftStickX()), "left_stick_right"),
    RIGHT_STICK_FORWARD((state, controller) -> -Math.min(0, state.axes().rightStickY()), "right_stick_up"),
    RIGHT_STICK_BACKWARD((state, controller) -> Math.max(0, state.axes().rightStickY()), "right_stick_down"),
    RIGHT_STICK_LEFT((state, controller) -> -Math.min(0, state.axes().rightStickX()), "right_stick_left"),
    RIGHT_STICK_RIGHT((state, controller) -> Math.max(0, state.axes().rightStickX()), "right_stick_right"),
    NONE((state, controller) -> 0f, "none");

    private final BiFunction<ControllerState, Controller, Float> state;
    private final String identifier;

    Bind(BiFunction<ControllerState, Controller, Float> state, String identifier) {
        this.state = state;
        this.identifier = identifier;
    }

    Bind(Function<ControllerState, Boolean> state, String identifier) {
        this((state1, controller) -> state.apply(state1) ? 1f : 0f, identifier);
    }

    @Override
    public float state(ControllerState state, Controller controller) {
        return this.state.apply(state, controller);
    }

    @Override
    public void draw(PoseStack matrices, int x, int centerY, Controller controller) {
        if (this != NONE)
            ButtonRenderer.drawButton(this, controller, matrices, x, centerY);
    }

    @Override
    public ButtonRenderer.DrawSize drawSize() {
        if (this == NONE) return new ButtonRenderer.DrawSize(0, 0);

        return new ButtonRenderer.DrawSize(22, 22);
    }

    public String identifier() {
        return identifier;
    }

    public ResourceLocation textureLocation(Controller controller) {
        return new ResourceLocation("controlify", "textures/gui/buttons/" + controller.config().theme.id() + "/" + identifier + ".png");
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(identifier);
    }

    public static Bind fromIdentifier(String identifier) {
        for (Bind bind : values()) {
            if (bind.identifier.equals(identifier)) return bind;
        }
        return null;
    }
}
