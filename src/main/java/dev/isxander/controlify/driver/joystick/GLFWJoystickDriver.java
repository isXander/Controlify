package dev.isxander.controlify.driver.joystick;

import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.driver.GUIDProvider;
import dev.isxander.controlify.driver.NameProviderDriver;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GLFWJoystickDriver implements BasicJoystickInputDriver, NameProviderDriver, GUIDProvider {
    private final int jid;
    private final String guid;
    private final String name;

    private BasicJoystickState state = BasicJoystickState.EMPTY;

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

        float[] axes = new float[numAxes];
        for (int i = 0; i < numAxes; i++) {
            axes[i] = axesBuf.get(i);
        }

        boolean[] buttons = new boolean[numButtons];
        for (int i = 0; i < numButtons; i++) {
            buttons[i] = buttonsBuf.get(i) == GLFW.GLFW_PRESS;
        }

        JoystickState.HatState[] hats = new JoystickState.HatState[numHats];
        for (int i = 0; i < numHats; i++) {
            hats[i] = switch (hatsBuf.get(i)) {
                case GLFW.GLFW_HAT_CENTERED -> JoystickState.HatState.CENTERED;
                case GLFW.GLFW_HAT_UP -> JoystickState.HatState.UP;
                case GLFW.GLFW_HAT_RIGHT -> JoystickState.HatState.RIGHT;
                case GLFW.GLFW_HAT_DOWN -> JoystickState.HatState.DOWN;
                case GLFW.GLFW_HAT_LEFT -> JoystickState.HatState.LEFT;
                case GLFW.GLFW_HAT_RIGHT_UP -> JoystickState.HatState.RIGHT_UP;
                case GLFW.GLFW_HAT_RIGHT_DOWN -> JoystickState.HatState.RIGHT_DOWN;
                case GLFW.GLFW_HAT_LEFT_UP -> JoystickState.HatState.LEFT_UP;
                case GLFW.GLFW_HAT_LEFT_DOWN -> JoystickState.HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + hatsBuf.get(i));
            };
        }

        this.state = new BasicJoystickState(buttons, axes, hats);
    }

    @Override
    public BasicJoystickState getBasicJoystickState() {
        return state;
    }

    @Override
    public int getNumAxes() {
        return 0;
    }

    @Override
    public int getNumButtons() {
        return 0;
    }

    @Override
    public int getNumHats() {
        return 0;
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
    public String getBasicJoystickDetails() {
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
