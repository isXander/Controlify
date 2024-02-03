package dev.isxander.controlify.driver.gamepad;

import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controller.composable.impl.ComposableControllerStateImpl;
import dev.isxander.controlify.controller.composable.gamepad.GamepadInputs;
import dev.isxander.controlify.driver.GUIDProvider;
import dev.isxander.controlify.driver.InputDriver;
import dev.isxander.controlify.driver.NameProviderDriver;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class GLFWGamepadDriver implements InputDriver, NameProviderDriver, GUIDProvider {
    private final int jid;
    private final String guid;

    private ComposableControllerState state = ComposableControllerState.EMPTY;

    public GLFWGamepadDriver(int jid) {
        this.jid = jid;
        this.guid = GLFW.glfwGetJoystickGUID(jid);
    }

    @Override
    public void update() {
        GLFWGamepadState glfwState = GLFWGamepadState.create();
        GLFW.glfwGetGamepadState(jid, glfwState);

        ComposableControllerStateImpl state = new ComposableControllerStateImpl();

        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)));

        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y)));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X)));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y)));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X)));

        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, (1f + glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER)) / 2f);
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, (1f + glfwState.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2f);

        state.setButton(GamepadInputs.SOUTH_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.EAST_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_B) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.WEST_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_X) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.NORTH_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_Y) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.BACK_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.START_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.GUIDE_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE) == GLFW.GLFW_PRESS);

        state.setButton(GamepadInputs.DPAD_UP_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == GLFW.GLFW_PRESS);
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, glfwState.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == GLFW.GLFW_PRESS);

        this.state = state;
    }

    @Override
    public int numButtons() {
        return 15;
    }

    @Override
    public int numAxes() {
        return 10;
    }

    @Override
    public int numHats() {
        return 0;
    }

    private float positiveAxis(float value) {
        return value < 0 ? 0 : value;
    }

    private float negativeAxis(float value) {
        return value > 0 ? 0 : -value;
    }

    @Override
    public ComposableControllerState getInputState() {
        return state;
    }

    @Override
    public String getInputDriverDetails() {
        return "GLFW Gamepad";
    }

    @Override
    public String getName() {
        String name = GLFW.glfwGetGamepadName(jid);
        // For some reason joystick name bypasses XInput abstractions.
        // In my case, joystick returns 'Wireless Xbox Controller'.
        if (name == null || name.startsWith("XInput")) {
            return GLFW.glfwGetJoystickName(jid);
        }
        return name;
    }

    @Override
    public String getNameProviderDetails() {
        return "GLFW Gamepad";
    }

    @Override
    public String getGUID() {
        return guid;
    }

    @Override
    public String getGUIDProviderDetails() {
        return "GLFW Gamepad";
    }

    @Override
    public boolean isGyroSupported() {
        return false;
    }
}
