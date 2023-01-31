package dev.isxander.controlify.controller;

import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.event.ControlifyEvents;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Controller {
    public static final Map<Integer, Controller> CONTROLLERS = new HashMap<>();

    private final int id;
    private final String guid;
    private final String name;
    private final boolean gamepad;

    private ControllerState state = ControllerState.EMPTY;
    private ControllerState prevState = ControllerState.EMPTY;

    private final ControllerBindings bindings = new ControllerBindings(this);

    public Controller(int id, String guid, String name, boolean gamepad) {
        this.id = id;
        this.guid = guid;
        this.name = name;
        this.gamepad = gamepad;
    }

    public ControllerState state() {
        return state;
    }

    public ControllerState prevState() {
        return prevState;
    }

    public void updateState() {
        if (!connected()) {
            state = prevState = ControllerState.EMPTY;
            return;
        }

        prevState = state;

        AxesState axesState = AxesState.fromController(this)
                .leftJoystickDeadZone(0.2f, 0.2f)
                .rightJoystickDeadZone(0.2f, 0.2f)
                .leftTriggerDeadZone(0.1f)
                .rightTriggerDeadZone(0.1f);
        ButtonState buttonState = ButtonState.fromController(this);
        state = new ControllerState(axesState, buttonState);

        ControlifyEvents.CONTROLLER_STATE_UPDATED.invoker().onControllerStateUpdate(this);
    }

    public ControllerBindings bindings() {
        return bindings;
    }

    public boolean connected() {
        return GLFW.glfwJoystickPresent(id);
    }

    GLFWGamepadState getGamepadState() {
        GLFWGamepadState state = GLFWGamepadState.create();
        if (gamepad)
            GLFW.glfwGetGamepadState(id, state);
        return state;
    }

    public int id() {
        return id;
    }

    public String guid() {
        return guid;
    }

    public String name() {
        return name;
    }

    public boolean gamepad() {
        return gamepad;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Controller) obj;
        return this.id == that.id &&
                Objects.equals(this.guid, that.guid) &&
                Objects.equals(this.name, that.name) &&
                this.gamepad == that.gamepad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid);
    }

    @Override
    public String toString() {
        return "Controller[" +
                "id=" + id + ", " +
                "name=" + name + ']';
    }

    public static Controller byId(int id) {
        if (id > GLFW.GLFW_JOYSTICK_LAST)
            throw new IllegalArgumentException("Invalid joystick id: " + id);
        if (CONTROLLERS.containsKey(id))
            return CONTROLLERS.get(id);

        String guid = GLFW.glfwGetJoystickGUID(id);
        boolean gamepad = GLFW.glfwJoystickIsGamepad(id);
        String name = gamepad ? GLFW.glfwGetGamepadName(id) : GLFW.glfwGetJoystickName(id);
        if (name == null) name = Integer.toString(id);

        Controller controller = new Controller(id, guid, name, gamepad);
        CONTROLLERS.put(id, controller);

        return controller;
    }

}
