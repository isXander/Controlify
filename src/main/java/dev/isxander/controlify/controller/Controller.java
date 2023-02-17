package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.debug.DebugProperties;
import org.hid4java.HidDevice;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public interface Controller<S extends ControllerState, C extends ControllerConfig> {
    String uid();
    int joystickId();
    String guid();

    ControllerBindings<S> bindings();

    S state();
    S prevState();

    C config();
    C defaultConfig();
    void resetConfig();
    void setConfig(Gson gson, JsonElement json);

    ControllerType type();

    String name();

    void updateState();

    Map<Integer, Controller<?, ?>> CONTROLLERS = new HashMap<>();

    static Controller<?, ?> createOrGet(int joystickId, @Nullable HidDevice device) {
        if (CONTROLLERS.containsKey(joystickId)) {
            return CONTROLLERS.get(joystickId);
        }

        if (GLFW.glfwJoystickIsGamepad(joystickId) && !DebugProperties.FORCE_JOYSTICK) {
            GamepadController controller = new GamepadController(joystickId, device);
            CONTROLLERS.put(joystickId, controller);
            return controller;
        }

        JoystickController controller = new JoystickController(joystickId, device);
        CONTROLLERS.put(joystickId, controller);
        return controller;
    }

    Controller<?, ?> DUMMY = new Controller<>() {
        private final ControllerBindings<ControllerState> bindings = new ControllerBindings<>(this);
        private final ControllerConfig config = new ControllerConfig() {
            @Override
            public void setDeadzone(int axis, float deadzone) {

            }

            @Override
            public float getDeadzone(int axis) {
                return 0;
            }
        };

        @Override
        public String uid() {
            return "NONE";
        }

        @Override
        public int joystickId() {
            return -1;
        }

        @Override
        public String guid() {
            return "DUMMY";
        }

        @Override
        public ControllerBindings<ControllerState> bindings() {
            return bindings;
        }

        @Override
        public ControllerConfig config() {
            return config;
        }

        @Override
        public ControllerConfig defaultConfig() {
            return config;
        }

        @Override
        public void resetConfig() {

        }

        @Override
        public void setConfig(Gson gson, JsonElement json) {

        }

        @Override
        public ControllerType type() {
            return ControllerType.UNKNOWN;
        }

        @Override
        public String name() {
            return "DUMMY";
        }

        @Override
        public ControllerState state() {
            return ControllerState.EMPTY;
        }

        @Override
        public ControllerState prevState() {
            return ControllerState.EMPTY;
        }

        @Override
        public void updateState() {

        }
    };
}
