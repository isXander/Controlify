package dev.isxander.controlify.controller.joystick.mapping;

import dev.isxander.controlify.bindings.JoystickAxisBind;
import net.minecraft.network.chat.Component;

public class UnmappedJoystickMapping implements JoystickMapping {
    public static final UnmappedJoystickMapping INSTANCE = new UnmappedJoystickMapping();

    @Override
    public Axis axis(int axis) {
        return new UnmappedAxis(axis);
    }

    @Override
    public Button button(int button) {
        return new UnmappedButton(button);
    }

    @Override
    public Hat hat(int hat) {
        return new UnmappedHat(hat);
    }

    private record UnmappedAxis(int axis) implements Axis {

        @Override
        public String identifier() {
                return "axis-" + axis;
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping.unmapped.axis", axis + 1);
        }

        @Override
        public boolean requiresDeadzone() {
            return true;
        }

        @Override
        public float modifyAxis(float value) {
            return value;
        }

        @Override
        public boolean isAxisResting(float value) {
            return value == restingValue();
        }

        @Override
        public float restingValue() {
            return 0;
        }

        @Override
        public String getDirectionIdentifier(int axis, JoystickAxisBind.AxisDirection direction) {
            return direction.name().toLowerCase();
        }
    }

    private record UnmappedButton(int button) implements Button {
        @Override
        public String identifier() {
            return "button-" + button;
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping.unmapped.button", button + 1);
        }
    }

    private record UnmappedHat(int hat) implements Hat {
        @Override
        public String identifier() {
            return "hat-" + hat;
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping.unmapped.hat", hat + 1);
        }
    }
}
