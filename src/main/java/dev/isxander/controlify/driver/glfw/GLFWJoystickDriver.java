package dev.isxander.controlify.driver.glfw;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerInfo;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import org.apache.commons.lang3.Validate;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Optional;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWJoystickDriver implements Driver {
    private final int jid;
    private final String guid;
    private final String name;
    private final int numButtons, numAxes, numHats;

    private final ControllerEntity controller;

    public GLFWJoystickDriver(int jid, ControllerType type, String uid, UniqueControllerID ucid, Optional<HIDDevice> hid) {
        this.jid = jid;
        this.guid = glfwGetJoystickGUID(jid);
        this.name = glfwGetJoystickName(jid);

        ControllerInfo info = new ControllerInfo(uid, ucid, this.guid, this.name, type, hid);
        this.controller = new ControllerEntity(info);

        GLFWJoystickState testState = this.getJoystickState();
        this.numButtons = testState.buttons().limit();
        this.numAxes = testState.axes().limit();
        this.numHats = testState.hats().limit();

        this.controller.setComponent(new InputComponent(this.controller, numButtons, numAxes * 2, numHats, false, Set.of(), type.mappingId()), InputComponent.ID);

        this.controller.finalise();
    }

    @Override
    public void update(boolean outOfFocus) {
        this.updateInput();
    }

    @Override
    public void close() {

    }

    @Override
    public ControllerEntity getController() {
        return controller;
    }

    private void updateInput() {
        GLFWJoystickState glfwState = this.getJoystickState();

        ControllerStateImpl state = new ControllerStateImpl();

        for (int i = 0; i < numAxes; i++) {
            float axis = glfwState.axes().get(i);
            state.setAxis(JoystickInputs.axis(i, true), Math.max(axis, 0));
            state.setAxis(JoystickInputs.axis(i, false), -Math.min(axis, 0));
        }

        for (int i = 0; i < numButtons; i++) {
            state.setButton(JoystickInputs.button(i), glfwState.buttons().get(i) == org.lwjgl.glfw.GLFW.GLFW_PRESS);
        }

        for (int i = 0; i < numHats; i++) {
            HatState hatState = switch (glfwState.hats().get(i)) {
                case GLFW.GLFW_HAT_CENTERED -> HatState.CENTERED;
                case GLFW.GLFW_HAT_UP -> HatState.UP;
                case GLFW.GLFW_HAT_RIGHT -> HatState.RIGHT;
                case GLFW.GLFW_HAT_DOWN -> HatState.DOWN;
                case GLFW.GLFW_HAT_LEFT -> HatState.LEFT;
                case GLFW.GLFW_HAT_RIGHT_UP -> HatState.RIGHT_UP;
                case GLFW.GLFW_HAT_RIGHT_DOWN -> HatState.RIGHT_DOWN;
                case GLFW.GLFW_HAT_LEFT_UP -> HatState.LEFT_UP;
                case GLFW.GLFW_HAT_LEFT_DOWN -> HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + glfwState.hats().get(i));
            };

            state.setHat(JoystickInputs.hat(i), hatState);
        }

        this.controller.input().orElseThrow().pushState(state);
    }

    private GLFWJoystickState getJoystickState() {
        ByteBuffer buttonsBuf = glfwGetJoystickButtons(jid);
        FloatBuffer axesBuf = glfwGetJoystickAxes(jid);
        ByteBuffer hatsBuf = glfwGetJoystickHats(jid);

        Validate.notNull(buttonsBuf, "Could not fetch buttons state for joystick");
        Validate.notNull(axesBuf, "Could not fetch axes state for joystick");
        Validate.notNull(hatsBuf, "Could not fetch  hat state for joystick");

        return new GLFWJoystickState(buttonsBuf, axesBuf, hatsBuf);
    }

    private record GLFWJoystickState(ByteBuffer buttons, FloatBuffer axes, ByteBuffer hats) {
    }
}
