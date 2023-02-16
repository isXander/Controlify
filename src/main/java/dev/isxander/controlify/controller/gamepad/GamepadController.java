package dev.isxander.controlify.controller.gamepad;

import dev.isxander.controlify.controller.AbstractController;
import org.hid4java.HidDevice;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class GamepadController extends AbstractController<GamepadState, GamepadConfig> {
    private GamepadState state = GamepadState.EMPTY;
    private GamepadState prevState = GamepadState.EMPTY;

    public GamepadController(int joystickId, HidDevice hidDevice) {
        super(joystickId, hidDevice);
        if (!GLFW.glfwJoystickIsGamepad(joystickId))
            throw new IllegalArgumentException("Joystick " + joystickId + " is not a gamepad!");

        if (!this.name.startsWith(type().friendlyName()))
            setName(GLFW.glfwGetGamepadName(joystickId));

        this.defaultConfig = new GamepadConfig();
        this.config = new GamepadConfig();
    }

    @Override
    public GamepadState state() {
        return state;
    }

    @Override
    public GamepadState prevState() {
        return prevState;
    }

    @Override
    public void updateState() {
        prevState = state;

        GamepadState.AxesState rawAxesState = GamepadState.AxesState.fromController(this);
        GamepadState.AxesState axesState = rawAxesState
                .leftJoystickDeadZone(config().leftStickDeadzoneX, config().leftStickDeadzoneY)
                .rightJoystickDeadZone(config().rightStickDeadzoneX, config().rightStickDeadzoneY);
        GamepadState.ButtonState buttonState = GamepadState.ButtonState.fromController(this);
        state = new GamepadState(axesState, rawAxesState, buttonState);
    }

    public void consumeButtonState() {
        this.state = new GamepadState(state().gamepadAxes(), state().rawGamepadAxes(), GamepadState.ButtonState.EMPTY);
    }

    GLFWGamepadState getGamepadState() {
        GLFWGamepadState state = GLFWGamepadState.create();
        GLFW.glfwGetGamepadState(joystickId(), state);
        return state;
    }

}
