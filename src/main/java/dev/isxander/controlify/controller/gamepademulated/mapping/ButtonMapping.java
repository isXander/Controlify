package dev.isxander.controlify.controller.gamepademulated.mapping;

import com.google.gson.*;
import dev.isxander.controlify.driver.joystick.BasicJoystickState;
import dev.isxander.controlify.controller.joystick.JoystickState;

import java.lang.reflect.Type;
import java.util.function.Function;

public sealed interface ButtonMapping permits ButtonMapping.FromButton, ButtonMapping.FromAxis, ButtonMapping.FromHat, ButtonMapping.FromNothing {
    boolean mapButton(BasicJoystickState state);

    MapOrigin origin();

    record FromButton(int button, boolean invert, MapOrigin origin) implements ButtonMapping {
        public FromButton(int button, boolean invert) {
            this(button, invert, MapOrigin.BUTTON);
        }

        @Override
        public boolean mapButton(BasicJoystickState state) {
            boolean input = state.buttons()[button];
            if (invert) input = !input;
            return input;
        }
    }

    record FromAxis(int axis, Function<Float, Boolean> valueMap, MapOrigin origin) implements ButtonMapping {
        public FromAxis(int axis, Function<Float, Boolean> valueMap) {
            this(axis, valueMap, MapOrigin.AXIS);
        }

        @Override
        public boolean mapButton(BasicJoystickState state) {
            float input = state.axes()[axis];
            return valueMap.apply(input);
        }

        public static final Function<Float, Boolean> NORMAL = f -> f >= 0.5f;
        public static final Function<Float, Boolean> INVERTED = f -> f <= 0.5f;
    }

    record FromHat(int hat, JoystickState.HatState targetState, boolean invert, MapOrigin origin) implements ButtonMapping {
        public FromHat(int hat, JoystickState.HatState targetState, boolean invert) {
            this(hat, targetState, invert, MapOrigin.HAT);
        }

        @Override
        public boolean mapButton(BasicJoystickState state) {
            boolean input = state.hats()[hat] == targetState;
            if (invert) input = !input;
            return input;
        }
    }

    record FromNothing(boolean def, MapOrigin origin) implements ButtonMapping {
        public FromNothing(boolean def) {
            this(def, MapOrigin.NOTHING);
        }

        @Override
        public boolean mapButton(BasicJoystickState state) {
            return def;
        }
    }

    class ButtonMappingSerializer implements JsonDeserializer<ButtonMapping> {
        @Override
        public ButtonMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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
