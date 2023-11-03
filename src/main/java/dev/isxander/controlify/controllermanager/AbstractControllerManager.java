package dev.isxander.controlify.controllermanager;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.joystick.CompoundJoystickController;
import dev.isxander.controlify.controller.joystick.SingleJoystickController;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.DebugLog;
import dev.isxander.controlify.utils.Log;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

public abstract class AbstractControllerManager implements ControllerManager {
    protected final Controlify controlify;
    protected final Minecraft minecraft;
    private final Map<String, Controller<?, ?>> CONTROLLERS = new HashMap<>();

    public AbstractControllerManager() {
        this.controlify = Controlify.instance();
        this.minecraft = Minecraft.getInstance();

        minecraft.getResourceManager()
                .getResource(Controlify.id("controllers/gamecontrollerdb.txt"))
                .ifPresent(this::loadGamepadMappings);
    }

    public Optional<Controller<?, ?>> createOrGet(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        try {
            Optional<String> uid = hidInfo.createControllerUID();
            if (uid.isPresent() && CONTROLLERS.containsKey(uid.get())) {
                return Optional.of(CONTROLLERS.get(uid.get()));
            }

            if (hidInfo.type().dontLoad()) {
                DebugLog.log("Preventing load of controller #" + joystickId + " because its type prevents loading.");
                return Optional.empty();
            }

            if (this.isControllerGamepad(joystickId) && !DebugProperties.FORCE_JOYSTICK && !hidInfo.type().forceJoystick()) {
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
            category.setDetail("HID path", hidInfo.hidDevice().map(HIDDevice::path).orElse("N/A"));
            category.setDetail("HID service status", Controlify.instance().controllerHIDService().isDisabled() ? "Disabled" : "Enabled");
            category.setDetail("GLFW name", Optional.ofNullable(getControllerSystemName(joystickId)).orElse("N/A"));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void tick(boolean outOfFocus) {
        for (var controller : this.getConnectedControllers()) {
            if (!outOfFocus)
                wrapControllerError(controller::updateState, "Updating controller state", controller);
            else
                wrapControllerError(controller::clearState, "Clearing controller state", controller);
            ControlifyEvents.CONTROLLER_STATE_UPDATE.invoker().onControllerStateUpdate(controller);
        }
    }

    @Override
    public Optional<Controller<?, ?>> getController(int joystickId) {
        return CONTROLLERS.values().stream().filter(controller -> controller.joystickId() == joystickId).findAny();
    }

    protected void onControllerConnected(Controller<?, ?> controller, boolean hotplug) {
        Log.LOGGER.info("Controller connected: {}", ControllerUtils.createControllerString(controller));

        boolean newController = controlify.config().loadOrCreateControllerData(controller);

        ControlifyEvents.CONTROLLER_CONNECTED.invoker().onControllerConnected(controller, hotplug, newController);
    }

    protected void onControllerRemoved(Controller<?, ?> controller) {
        Log.LOGGER.info("Controller disconnected: {}", ControllerUtils.createControllerString(controller));

        controller.hidInfo().ifPresent(controlify.controllerHIDService()::unconsumeController);
        removeController(controller);

        ControlifyEvents.CONTROLLER_DISCONNECTED.invoker().onControllerDisconnected(controller);
    }

    protected void removeController(Controller<?, ?> controller) {
        controller.close();
        CONTROLLERS.remove(controller.uid(), controller);

        checkCompoundJoysticks();
    }

    protected void removeController(String uid) {
        Controller<?, ?> prev = CONTROLLERS.remove(uid);
        if (prev != null) {
            prev.close();
        }

        checkCompoundJoysticks();
    }

    @Override
    public List<Controller<?, ?>> getConnectedControllers() {
        return ImmutableList.copyOf(CONTROLLERS.values());
    }

    @Override
    public boolean isControllerConnected(String uid) {
        return CONTROLLERS.containsKey(uid);
    }

    @Override
    public void close() {
        CONTROLLERS.values().forEach(Controller::close);
    }

    protected abstract void loadGamepadMappings(Resource resource);

    protected abstract String getControllerSystemName(int joystickId);

    private void checkCompoundJoysticks() {
        Controlify.instance().config().getCompoundJoysticks().values().forEach(info -> {
            try {
                if (info.isLoaded() && !info.canBeUsed()) {
                    Log.LOGGER.warn("Unloading compound joystick " + info.friendlyName() + " due to missing controllers.");
                    removeController(info.type().mappingId());
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
