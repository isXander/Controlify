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

    Map<String, Controller<?, ?>> CONTROLLERS = new HashMap<>();

    static Optional<Controller<?, ?>> createOrGet(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        try {
            Optional<String> uid = hidInfo.createControllerUID();
            if (uid.isPresent() && CONTROLLERS.containsKey(uid.get())) {
                return Optional.of(CONTROLLERS.get(uid.get()));
            }

            if (hidInfo.type().dontLoad()) {
                DebugLog.log("Preventing load of controller #" + joystickId + " because its type prevents loading.");
                return Optional.empty();
            }

            if (GLFW.glfwJoystickIsGamepad(joystickId) && !DebugProperties.FORCE_JOYSTICK && !hidInfo.type().forceJoystick()) {
                GamepadController controller = new GamepadController(joystickId, hidInfo);
                CONTROLLERS.put(controller.uid(), controller);
                return Optional.of(controller);
            }

            SingleJoystickController controller = new SingleJoystickController(joystickId, hidInfo);
            CONTROLLERS.put(controller.uid(), controller);
            return Optional.of(controller);
        } catch (Throwable e) {
            CrashReport crashReport = CrashReport.forThrowable(e, "Creating controller #" + joystickId);
            CrashReportCategory category = crashReport.addCategory("Controller Info");
            category.setDetail("Joystick ID", joystickId);
            category.setDetail("Controller identification", hidInfo.type());
            category.setDetail("HID path", hidInfo.hidDevice().map(HidDevice::getPath).orElse("N/A"));
            category.setDetail("HID service status", Controlify.instance().controllerHIDService().isDisabled() ? "Disabled" : "Enabled");
            category.setDetail("GLFW name", Optional.ofNullable(GLFW.glfwGetJoystickName(joystickId)).orElse("N/A"));
            throw new ReportedException(crashReport);
        }
    }

    static void remove(Controller<?, ?> controller) {
        controller.close();
        CONTROLLERS.remove(controller.uid(), controller);
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
