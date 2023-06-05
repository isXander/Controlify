package dev.isxander.controlify.controller.joystick;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.IntStream;

public class JoystickState implements ControllerState {
    public static final JoystickState EMPTY = new JoystickState(UnmappedJoystickMapping.EMPTY, List.of(), List.of(), List.of(), List.of());

    private final JoystickMapping mapping;

    private final List<Float> axes;
    private final List<Float> rawAxes;
    private final List<Boolean> buttons;
    private final List<HatState> hats;

    protected JoystickState(JoystickMapping mapping, List<Float> axes, List<Float> rawAxes, List<Boolean> buttons, List<HatState> hats) {
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
        return IntStream.range(0, axes().size()).anyMatch(i -> !mapping.axes()[i].isAxisResting(axes().get(i)))
                || buttons().stream().anyMatch(Boolean::booleanValue)
                || hats().stream().anyMatch(hat -> hat != HatState.CENTERED);
    }

    @Override
    public String toString() {
        return "JoystickState{" +
                "axes=" + axes +
                ", rawAxes=" + rawAxes +
                ", buttons=" + buttons +
                ", hats=" + hats +
                '}';
    }

    public static JoystickState fromJoystick(JoystickController<?> joystick, int joystickId) {
        Optional<FloatBuffer> axesBuffer = Optional.ofNullable(GLFW.glfwGetJoystickAxes(joystickId));
        float[] inAxes = new float[axesBuffer.map(FloatBuffer::limit).orElse(0)];
        {
            int i = 0;
            while (axesBuffer.isPresent() && axesBuffer.get().hasRemaining()) {
                inAxes[i] = axesBuffer.get().get();
                i++;
            }
        }

        Optional<ByteBuffer> buttonBuffer = Optional.ofNullable(GLFW.glfwGetJoystickButtons(joystickId));
        boolean[] inButtons = new boolean[buttonBuffer.map(ByteBuffer::limit).orElse(0)];
        {
            int i = 0;
            while (buttonBuffer.isPresent() && buttonBuffer.get().hasRemaining()) {
                inButtons[i] = buttonBuffer.get().get() == GLFW.GLFW_PRESS;
                i++;
            }
        }

        Optional<ByteBuffer> hatBuffer = Optional.ofNullable(GLFW.glfwGetJoystickHats(joystickId));
        HatState[] inHats = new HatState[hatBuffer.map(ByteBuffer::limit).orElse(0)];
        {
            int i = 0;
            while (hatBuffer.isPresent() && hatBuffer.get().hasRemaining()) {
                var state = switch (hatBuffer.get().get()) {
                    case GLFW.GLFW_HAT_CENTERED -> HatState.CENTERED;
                    case GLFW.GLFW_HAT_UP -> HatState.UP;
                    case GLFW.GLFW_HAT_RIGHT -> HatState.RIGHT;
                    case GLFW.GLFW_HAT_DOWN -> HatState.DOWN;
                    case GLFW.GLFW_HAT_LEFT -> HatState.LEFT;
                    case GLFW.GLFW_HAT_RIGHT_UP -> HatState.RIGHT_UP;
                    case GLFW.GLFW_HAT_RIGHT_DOWN -> HatState.RIGHT_DOWN;
                    case GLFW.GLFW_HAT_LEFT_UP -> HatState.LEFT_UP;
                    case GLFW.GLFW_HAT_LEFT_DOWN -> HatState.LEFT_DOWN;
                    default -> throw new IllegalStateException("Unexpected value: " + hatBuffer.get());
                };
                inHats[i] = state;
            }
        }

        JoystickMapping.JoystickData data = new JoystickMapping.JoystickData(inAxes, inButtons, inHats);
        JoystickMapping mapping = joystick.mapping();

        JoystickMapping.Axis[] axes = mapping.axes();
        List<Float> rawAxes = new ArrayList<>(axes.length);
        List<Float> deadzoneAxes = new ArrayList<>(axes.length);
        for (int i = 0; i < axes.length; i++) {
            var axis = axes[i];
            float state = axis.getAxis(data);
            rawAxes.add(state);
            deadzoneAxes.add(axis.requiresDeadzone()
                    ? ControllerUtils.deadzone(state, joystick.config().getDeadzone(i))
                    : state
            );
        }

        List<Boolean> buttons = Arrays.stream(mapping.buttons()).map(button -> button.isPressed(data)).toList();
        List<HatState> hats = Arrays.stream(mapping.hats()).map(hat -> hat.getHatState(data)).toList();

        if (DebugProperties.PRINT_JOY_STATE) {
            Controlify.LOGGER.info("Printing joystick state for controller {}", joystick);
            Controlify.LOGGER.info(Arrays.stream(axes).map(axis -> axis.name().getString() + ": " + axis.getAxis(data)).toList().toString());
            Controlify.LOGGER.info(Arrays.stream(mapping.buttons()).map(button -> button.name().getString() + ": " + button.isPressed(data)).toList().toString());
            Controlify.LOGGER.info(Arrays.stream(mapping.hats()).map(hat -> hat.name().getString() + ": " + hat.getHatState(data)).toList().toString());
        }

        return new JoystickState(joystick.mapping(), deadzoneAxes, rawAxes, buttons, hats);
    }

    public static JoystickState empty(JoystickController<?> joystick) {
        var axes = Arrays.stream(joystick.mapping().axes()).map(JoystickMapping.Axis::restingValue).toList();
        var buttons = IntStream.range(0, joystick.mapping().buttons().length).mapToObj(i -> false).toList();
        var hats = IntStream.range(0, joystick.mapping().hats().length).mapToObj(i -> HatState.CENTERED).toList();

        return new JoystickState(joystick.mapping(), axes, axes, buttons, hats);
    }

    public static JoystickState merged(JoystickMapping mapping, Collection<JoystickState> states) {
        var axes = new ArrayList<Float>();
        var rawAxes = new ArrayList<Float>();
        var buttons = new ArrayList<Boolean>();
        var hats = new ArrayList<HatState>();

        for (var state : states) {
            axes.addAll(state.axes);
            rawAxes.addAll(state.rawAxes);
            buttons.addAll(state.buttons);
            hats.addAll(state.hats);
        }

        return new JoystickState(mapping, axes, rawAxes, buttons, hats);
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
