package dev.isxander.controlify.controller.joystick;

import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class JoystickState implements ControllerState {
    public static final JoystickState EMPTY = new JoystickState(UnmappedJoystickMapping.INSTANCE, List.of(), List.of(), List.of(), List.of());

    private final JoystickMapping mapping;

    private final List<Float> axes;
    private final List<Float> rawAxes;
    private final List<Boolean> buttons;
    private final List<HatState> hats;

    private JoystickState(JoystickMapping mapping, List<Float> axes, List<Float> rawAxes, List<Boolean> buttons, List<HatState> hats) {
        this.mapping = mapping;
        this.axes = axes;
        this.rawAxes = rawAxes;
        this.buttons = buttons;
        this.hats = hats;
    }

    @Override
    public List<Float> axes() {
        return axes;
    }

    @Override
    public List<Float> rawAxes() {
        return rawAxes;
    }

    @Override
    public List<Boolean> buttons() {
        return buttons;
    }

    public List<HatState> hats() {
        return hats;
    }

    @Override
    public boolean hasAnyInput() {
        return IntStream.range(0, axes().size()).anyMatch(i -> !mapping.axis(i).isAxisResting(axes().get(i)))
                || buttons().stream().anyMatch(Boolean::booleanValue)
                || hats().stream().anyMatch(hat -> hat != HatState.CENTERED);
    }

    public static JoystickState fromJoystick(JoystickController joystick) {
        FloatBuffer axesBuffer = GLFW.glfwGetJoystickAxes(joystick.joystickId());
        List<Float> axes = new ArrayList<>();
        List<Float> rawAxes = new ArrayList<>();
        if (axesBuffer != null) {
            int i = 0;
            while (axesBuffer.hasRemaining()) {
                var axisMapping = joystick.mapping().axis(i);
                var axis = axisMapping.modifyAxis(axesBuffer.get());
                var deadzone = axisMapping.requiresDeadzone();

                rawAxes.add(axis);
                axes.add(deadzone ? ControllerUtils.deadzone(axis, joystick.config().getDeadzone(i)) : axis);

                i++;
            }
        }

        ByteBuffer buttonBuffer = GLFW.glfwGetJoystickButtons(joystick.joystickId());
        List<Boolean> buttons = new ArrayList<>();
        if (buttonBuffer != null) {
            while (buttonBuffer.hasRemaining()) {
                buttons.add(buttonBuffer.get() == GLFW.GLFW_PRESS);
            }
        }

        ByteBuffer hatBuffer = GLFW.glfwGetJoystickHats(joystick.joystickId());
        List<JoystickState.HatState> hats = new ArrayList<>();
        if (hatBuffer != null) {
            while (hatBuffer.hasRemaining()) {
                var state = switch (hatBuffer.get()) {
                    case GLFW.GLFW_HAT_CENTERED -> JoystickState.HatState.CENTERED;
                    case GLFW.GLFW_HAT_UP -> JoystickState.HatState.UP;
                    case GLFW.GLFW_HAT_RIGHT -> JoystickState.HatState.RIGHT;
                    case GLFW.GLFW_HAT_DOWN -> JoystickState.HatState.DOWN;
                    case GLFW.GLFW_HAT_LEFT -> JoystickState.HatState.LEFT;
                    case GLFW.GLFW_HAT_RIGHT_UP -> JoystickState.HatState.RIGHT_UP;
                    case GLFW.GLFW_HAT_RIGHT_DOWN -> JoystickState.HatState.RIGHT_DOWN;
                    case GLFW.GLFW_HAT_LEFT_UP -> JoystickState.HatState.LEFT_UP;
                    case GLFW.GLFW_HAT_LEFT_DOWN -> JoystickState.HatState.LEFT_DOWN;
                    default -> throw new IllegalStateException("Unexpected value: " + hatBuffer.get());
                };
                hats.add(state);
            }
        }

        return new JoystickState(joystick.mapping(), axes, rawAxes, buttons, hats);
    }

    public enum HatState implements NameableEnum {
        CENTERED,
        UP,
        RIGHT,
        DOWN,
        LEFT,
        RIGHT_UP,
        RIGHT_DOWN,
        LEFT_UP,
        LEFT_DOWN;

        public boolean isCentered() {
            return this == CENTERED;
        }

        public boolean isRight() {
            return this == RIGHT || this == RIGHT_UP || this == RIGHT_DOWN;
        }

        public boolean isUp() {
            return this == UP || this == RIGHT_UP || this == LEFT_UP;
        }

        public boolean isLeft() {
            return this == LEFT || this == LEFT_UP || this == LEFT_DOWN;
        }

        public boolean isDown() {
            return this == DOWN || this == RIGHT_DOWN || this == LEFT_DOWN;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("controlify.hat_state." + this.name().toLowerCase());
        }
    }
}
