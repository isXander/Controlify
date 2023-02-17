package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadConfig;
import dev.isxander.controlify.controller.gamepad.BuiltinGamepadTheme;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum GamepadBind implements IBind<GamepadState> {
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
    private final Map<BuiltinGamepadTheme, ResourceLocation> textureLocations;

    GamepadBind(Function<GamepadState, Float> state, String identifier, boolean jvmIsBad) {
        this.state = state;
        this.identifier = identifier;

        this.textureLocations = new HashMap<>();
        for (BuiltinGamepadTheme theme : BuiltinGamepadTheme.values()) {
            if (theme == BuiltinGamepadTheme.DEFAULT) continue;
            textureLocations.put(theme, new ResourceLocation("controlify", "textures/gui/gamepad_buttons/" + theme.id() + "/" + identifier + ".png"));
        }
    }

    GamepadBind(Function<GamepadState, Boolean> state, String identifier) {
        this(state1 -> state.apply(state1) ? 1f : 0f, identifier, true);
    }

    @Override
    public float state(GamepadState state) {
        return this.state.apply(state);
    }

    @Override
    public void draw(PoseStack matrices, int x, int centerY, Controller<GamepadState, ?> controller) {
        ResourceLocation texture;
        if (((GamepadConfig)controller.config()).theme == BuiltinGamepadTheme.DEFAULT) {
            texture = new ResourceLocation("controlify", "textures/gui/gamepad/" + controller.type().identifier() + "/" + identifier + ".png");
        } else {
            texture = textureLocations.get(((GamepadConfig)controller.config()).theme);
        }

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiComponent.blit(matrices, x, centerY - 22 / 2, 0, 0, 22, 22, 22, 22);
    }

    @Override
    public DrawSize drawSize() {
        return new DrawSize(22, 22);
    }

    public String identifier() {
        return identifier;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("bind", identifier);
        return object;
    }

    public static GamepadBind fromJson(JsonObject object) {
        String name = object.get("bind").getAsString();
        for (GamepadBind bind : values()) {
            if (bind.identifier.equals(name)) return bind;
        }
        return null;
    }
}
