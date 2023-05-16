package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.joystick.SingleJoystickController;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;

public interface IBind<S extends ControllerState> {
    float state(S state);
    default boolean held(S state) {
        return state(state) > controller().config().buttonActivationThreshold;
    }

    void draw(GuiGraphics graphics, int x, int centerY);
    DrawSize drawSize();

    JsonObject toJson();

    Controller<S, ?> controller();

    @SuppressWarnings("unchecked")
    static <T extends ControllerState> IBind<T> fromJson(JsonObject json, Controller<T, ?> controller) {
        var type = json.get("type").getAsString();
        if (type.equals(EmptyBind.BIND_ID))
            return new EmptyBind<>();

        if (controller instanceof GamepadController gamepad && type.equals(GamepadBinds.BIND_ID)) {
            return GamepadBinds.fromJson(json).map(bind -> (IBind<T>) bind.forGamepad(gamepad)).orElse(new EmptyBind<>());
        } else if (controller instanceof SingleJoystickController joystick) {
            return (IBind<T>) switch (type) {
                case JoystickButtonBind.BIND_ID -> JoystickButtonBind.fromJson(json, joystick);
                case JoystickHatBind.BIND_ID -> JoystickHatBind.fromJson(json, joystick);
                case JoystickAxisBind.BIND_ID -> JoystickAxisBind.fromJson(json, joystick);
                default -> throw new IllegalStateException("Unknown bind type for joystick: " + type);
            };
        }

        throw new IllegalStateException("Unknown controller type: " + controller.getClass().getName());
    }
}
