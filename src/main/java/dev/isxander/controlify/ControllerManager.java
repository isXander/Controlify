package dev.isxander.controlify;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.controller.joystick.CompoundJoystickController;
import dev.isxander.controlify.controller.joystick.SingleJoystickController;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.utils.DebugLog;
import dev.isxander.controlify.utils.Log;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.hid4java.HidDevice;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ControllerManager {
    private ControllerManager() {
    }

    private final static Map<String, Controller<?, ?>> CONTROLLERS = new HashMap<>();

    public static Optional<Controller<?, ?>> createOrGet(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
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
                checkCompoundJoysticks();
                return Optional.of(controller);
            }

            SingleJoystickController controller = new SingleJoystickController(joystickId, hidInfo);
            CONTROLLERS.put(controller.uid(), controller);
            checkCompoundJoysticks();
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

    public static void disconnect(Controller<?, ?> controller) {
        controller.close();
        CONTROLLERS.remove(controller.uid(), controller);

        checkCompoundJoysticks();
    }

    public static void disconnect(String uid) {
        Controller<?, ?> prev = CONTROLLERS.remove(uid);
        if (prev != null) {
            prev.close();
        }

        checkCompoundJoysticks();
    }

    public static List<Controller<?, ?>> getConnectedControllers() {
        return ImmutableList.copyOf(CONTROLLERS.values());
    }

    public static boolean isControllerConnected(String uid) {
        return CONTROLLERS.containsKey(uid);
    }

    private static void checkCompoundJoysticks() {
        Controlify.instance().config().getCompoundJoysticks().values().forEach(info -> {
            try {
                if (info.isLoaded() && !info.canBeUsed()) {
                    Log.LOGGER.warn("Unloading compound joystick " + info.friendlyName() + " due to missing controllers.");
                    disconnect(info.type().mappingId());
                }

                if (!info.isLoaded() && info.canBeUsed()) {
                    Log.LOGGER.info("Loading compound joystick " + info.type().mappingId() + ".");
                    CompoundJoystickController controller = info.attemptCreate().orElseThrow();
                    CONTROLLERS.put(info.type().mappingId(), controller);
                    Controlify.instance().config().loadOrCreateControllerData(controller);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
