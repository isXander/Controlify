package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.controller.joystick.SingleJoystickController;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.utils.DebugLog;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.hid4java.HidDevice;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Controller<S extends ControllerState, C extends ControllerConfig> {
    String uid();
    int joystickId();

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
    void clearState();

    default void close() {}

    RumbleManager rumbleManager();
    boolean supportsRumble();

    Optional<ControllerHIDService.ControllerHIDInfo> hidInfo();

    default boolean canBeUsed() {
        return true;
    }

    @Deprecated
    Controller<?, ?> DUMMY = new Controller<>() {
        private final ControllerBindings<ControllerState> bindings = new ControllerBindings<>(this);
        private final RumbleManager rumbleManager = new RumbleManager(new RumbleCapable() {
            @Override
            public boolean setRumble(float strongMagnitude, float weakMagnitude, RumbleSource source) {
                return false;
            }

            @Override
            public boolean supportsRumble() {
                return false;
            }
        });
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
        public Optional<ControllerHIDService.ControllerHIDInfo> hidInfo() {
            return Optional.empty();
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

        @Override
        public void clearState() {

        }

        @Override
        public RumbleManager rumbleManager() {
            return rumbleManager;
        }

        @Override
        public boolean supportsRumble() {
            return false;
        }
    };
}
