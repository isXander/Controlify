package dev.isxander.controlify.driver.joystick;

import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controller.composable.HatState;
import dev.isxander.controlify.controller.composable.ModifiableControllerState;
import dev.isxander.controlify.controller.composable.impl.ComposableControllerStateImpl;
import dev.isxander.controlify.controller.composable.joystick.JoystickInputs;
import dev.isxander.controlify.driver.GUIDProvider;
import dev.isxander.controlify.driver.InputDriver;
import dev.isxander.controlify.driver.NameProviderDriver;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GLFWJoystickDriver implements InputDriver, NameProviderDriver, GUIDProvider {
    private final int jid;
    private final String guid;
    private final String name;

    private ComposableControllerState state = ComposableControllerState.EMPTY;

    public GLFWJoystickDriver(int jid) {
        this.jid = jid;
        this.guid = GLFW.glfwGetJoystickGUID(jid);
        this.name = GLFW.glfwGetJoystickName(jid);
    }

    @Override
    public void update() {
        FloatBuffer axesBuf = GLFW.glfwGetJoystickAxes(jid);
        ByteBuffer buttonsBuf = GLFW.glfwGetJoystickButtons(jid);
        ByteBuffer hatsBuf = GLFW.glfwGetJoystickHats(jid);

        Validate.notNull(axesBuf, "Could not fetch axes state for joystick");
        Validate.notNull(buttonsBuf, "Could not fetch buttons state for joystick");
        Validate.notNull(hatsBuf, "Could not fetch hats state for joystick");

        int numAxes = axesBuf.limit();
        int numButtons = buttonsBuf.limit();
        int numHats = hatsBuf.limit();

        ModifiableControllerState state = new ComposableControllerStateImpl();

        for (int i = 0; i < numAxes; i++) {
            state.setAxis(JoystickInputs.axis(i), axesBuf.get(i));
        }

        for (int i = 0; i < numButtons; i++) {
            state.setButton(JoystickInputs.button(i), buttonsBuf.get(i) == GLFW.GLFW_PRESS);
        }

        for (int i = 0; i < numHats; i++) {
            HatState hatState = switch (hatsBuf.get(i)) {
                case GLFW.GLFW_HAT_CENTERED -> HatState.CENTERED;
                case GLFW.GLFW_HAT_UP -> HatState.UP;
                case GLFW.GLFW_HAT_RIGHT -> HatState.RIGHT;
                case GLFW.GLFW_HAT_DOWN -> HatState.DOWN;
                case GLFW.GLFW_HAT_LEFT -> HatState.LEFT;
                case GLFW.GLFW_HAT_RIGHT_UP -> HatState.RIGHT_UP;
                case GLFW.GLFW_HAT_RIGHT_DOWN -> HatState.RIGHT_DOWN;
                case GLFW.GLFW_HAT_LEFT_UP -> HatState.LEFT_UP;
                case GLFW.GLFW_HAT_LEFT_DOWN -> HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + hatsBuf.get(i));
            };

            state.setHat(JoystickInputs.hat(i), hatState);
        }

        this.state = state;
    }

    @Override
    public ComposableControllerState getInputState() {
        return state;
    }

    @Override
    public int numButtons() {
        return state.getButtons().size();
    }

    @Override
    public int numAxes() {
        return state.getAxes().size();
    }

    @Override
    public int numHats() {
        return state.getHats().size();
    }

    @Override
    public boolean isGyroSupported() {
        return false;
    }

    @Override
    public String getGUID() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInputDriverDetails() {
        return "GLFWjoy";
    }

    @Override
    public String getNameProviderDetails() {
        return "GLFWjoy";
    }

    @Override
    public String getGUIDProviderDetails() {
        return "GLFWjoy";
    }
}
