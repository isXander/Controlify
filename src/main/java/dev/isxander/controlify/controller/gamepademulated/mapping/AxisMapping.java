package dev.isxander.controlify.controller.gamepademulated.mapping;

import com.google.gson.*;
import dev.isxander.controlify.driver.joystick.BasicJoystickState;
import dev.isxander.controlify.controller.joystick.JoystickState;

import java.lang.reflect.Type;

public sealed interface AxisMapping permits AxisMapping.FromButton, AxisMapping.FromAxis, AxisMapping.FromHat, AxisMapping.FromNothing {
    float mapAxis(BasicJoystickState state);

    MapOrigin origin();

    record FromButton(int button, float off, float on, MapOrigin origin) implements AxisMapping {
        public FromButton(int button, float off, float on) {
            this(button, off, on, MapOrigin.BUTTON);
        }

        @Override
        public float mapAxis(BasicJoystickState state) {
            boolean buttonState = state.buttons()[button];
            return buttonState ? on : off;
        }
    }

    record FromAxis(int axis, float inMin, float inMax, float outMin, float outMax, MapOrigin origin) implements AxisMapping {
        public FromAxis(int axis, float inMin, float inMax, float outMin, float outMax) {
            this(axis, inMin, inMax, outMin, outMax, MapOrigin.AXIS);
        }

        @Override
        public float mapAxis(BasicJoystickState state) {
            float input = state.axes()[axis];
            return (input + (outMin - inMin)) / (inMax - inMin) * (outMax - outMin);
        }
    }

    record FromHat(int hat, JoystickState.HatState hatState, float off, float on, MapOrigin origin) implements AxisMapping {
        public FromHat(int hat, JoystickState.HatState hatState, float off, float on) {
            this(hat, hatState, off, on, MapOrigin.HAT);
        }

        @Override
        public float mapAxis(BasicJoystickState state) {
            boolean digitalState = state.hats()[hat] == hatState;
            return digitalState ? on : off;
        }
    }

    record FromNothing(float resting, MapOrigin origin) implements AxisMapping {
        public FromNothing(float resting) {
            this(resting, MapOrigin.NOTHING);
        }

        @Override
        public float mapAxis(BasicJoystickState state) {
            return resting;
        }
    }

    class AxisMappingSerializer implements JsonDeserializer<AxisMapping> {
        @Override
        public AxisMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MapOrigin origin = context.deserialize(json.getAsJsonObject().get("origin"), MapOrigin.class);
            return switch (origin) {
                case BUTTON -> context.deserialize(json, FromButton.class);
                case AXIS -> context.deserialize(json, FromAxis.class);
                case HAT -> context.deserialize(json, FromHat.class);
                case NOTHING -> context.deserialize(json, FromNothing.class);
            };
        }
    }
}
