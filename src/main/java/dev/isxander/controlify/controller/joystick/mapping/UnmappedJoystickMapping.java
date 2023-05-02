package dev.isxander.controlify.controller.joystick.mapping;

import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.render.GenericRenderer;
import dev.isxander.controlify.controller.joystick.render.JoystickRenderer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class UnmappedJoystickMapping implements JoystickMapping {
    public static final UnmappedJoystickMapping EMPTY = new UnmappedJoystickMapping(0, 0, 0);

    private final UnmappedAxis[] axes;
    private final UnmappedButton[] buttons;
    private final UnmappedHat[] hats;

    private UnmappedJoystickMapping(int axisCount, int buttonCount, int hatCount) {
        this.axes = new UnmappedAxis[axisCount];
        for (int i = 0; i < axisCount; i++) {
            this.axes[i] = new UnmappedAxis(i, new GenericRenderer.Axis(Integer.toString(i + 1)));
        }

        this.buttons = new UnmappedButton[buttonCount];
        for (int i = 0; i < buttonCount; i++) {
            this.buttons[i] = new UnmappedButton(i, new GenericRenderer.Button(Integer.toString(i + 1)));
        }

        this.hats = new UnmappedHat[hatCount];
        for (int i = 0; i < hatCount; i++) {
            this.hats[i] = new UnmappedHat(i);
        }
    }

    public UnmappedJoystickMapping(int joystickId) {
        this(
                GLFW.glfwGetJoystickAxes(joystickId).limit(),
                GLFW.glfwGetJoystickButtons(joystickId).limit(),
                GLFW.glfwGetJoystickHats(joystickId).limit()
        );
    }

    @Override
    public Axis[] axes() {
        return axes;
    }

    @Override
    public Button[] buttons() {
        return buttons;
    }

    @Override
    public Hat[] hats() {
        return hats;
    }

    private record UnmappedAxis(int axis, GenericRenderer.Axis renderer) implements Axis {
        @Override
        public float getAxis(JoystickData data) {
            return data.axes()[axis];
        }

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

    private record UnmappedButton(int button, GenericRenderer.Button renderer) implements Button {
        @Override
        public boolean isPressed(JoystickData data) {
            return data.buttons()[button];
        }

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
        public JoystickState.HatState getHatState(JoystickData data) {
            return data.hats()[hat];
        }

        @Override
        public String identifier() {
            return "hat-" + hat;
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping.unmapped.hat", hat + 1);
        }

        @Override
        public JoystickRenderer renderer(JoystickState.HatState state) {
            return new GenericRenderer.Hat(Integer.toString(hat + 1));
        }
    }
}
