package dev.isxander.controlify.driver.gamepad;

import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.driver.GUIDProvider;
import dev.isxander.controlify.driver.NameProviderDriver;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class GLFWGamepadDriver implements BasicGamepadInputDriver, NameProviderDriver, GUIDProvider {
    private final int jid;
    private final String guid;

    private BasicGamepadState state = new BasicGamepadState(GamepadState.AxesState.EMPTY, GamepadState.ButtonState.EMPTY);

    public GLFWGamepadDriver(int jid) {
        this.jid = jid;
        this.guid = GLFW.glfwGetJoystickGUID(jid);
    }

    @Override
    public void update() {
        GLFWGamepadState state = GLFWGamepadState.create();
        GLFW.glfwGetGamepadState(jid, state);

        GamepadState.AxesState axes = new GamepadState.AxesState(
                state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X),
                state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y),
                state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X),
                state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y),
                (1f + state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER)) / 2f,
                (1f + state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2f
        );
        GamepadState.ButtonState buttons = new GamepadState.ButtonState(
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_B) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_X) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_Y) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB) == GLFW.GLFW_PRESS,
                state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB) == GLFW.GLFW_PRESS
        );

        this.state = new BasicGamepadState(axes, buttons);
    }

    @Override
    public BasicGamepadState getBasicGamepadState() {
        return state;
    }

    @Override
    public String getBasicGamepadDetails() {
        return "GLFW Gamepad";
    }

    @Override
    public String getName() {
        String name = GLFW.glfwGetGamepadName(jid);
        // For some reason joystick name bypasses XInput abstractions.
        // In my case, joystick returns 'Wireless Xbox Controller'.
        if ("XInput Gamepad (GLFW)".equals(name)) {
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
}
