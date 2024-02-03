package dev.isxander.controlify.controllermanager;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.DebugLog;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

public abstract class AbstractControllerManager implements ControllerManager {
    protected final Controlify controlify;
    protected final Minecraft minecraft;

    protected final Map<UniqueControllerID, Controller<?>> controllersByJid = new Object2ObjectOpenHashMap<>();
    protected final Map<String, Controller<?>> controllersByUid = new Object2ObjectOpenHashMap<>();
    protected final Map<String, ControllerHIDService.ControllerHIDInfo> hidInfoByUid = new Object2ObjectOpenHashMap<>();

    public AbstractControllerManager() {
        this.controlify = Controlify.instance();
        this.minecraft = Minecraft.getInstance();

        minecraft.getResourceManager()
                .getResource(Controlify.id("controllers/gamecontrollerdb.txt"))
                .ifPresent(this::loadGamepadMappings);
    }

    public Optional<Controller<?>> createOrGet(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo) {
        try {
            Optional<String> uid = hidInfo.createControllerUID();
            if (uid.isPresent() && controllersByUid.containsKey(uid.get())) {
                return Optional.of(controllersByUid.get(uid.get()));
            }

            if (hidInfo.type().dontLoad()) {
                DebugLog.log("Preventing load of controller #" + ucid + " because its type prevents loading.");
                return Optional.empty();
            }

            Optional<Controller<?>> controller = createController(ucid, hidInfo);
            controller.ifPresent(c -> addController(ucid, c));
            return controller;
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to create controller #" + ucid + "!", e);
            CrashReport crashReport = CrashReport.forThrowable(e, "Creating controller #" + ucid);
            CrashReportCategory category = crashReport.addCategory("Controller Info");
            category.setDetail("Unique controller ID", ucid);
            category.setDetail("Controller identification", hidInfo.type());
            category.setDetail("HID path", hidInfo.hidDevice().map(HIDDevice::path).orElse("N/A"));
            category.setDetail("HID service status", Controlify.instance().controllerHIDService().isDisabled() ? "Disabled" : "Enabled");
            category.setDetail("System name", Optional.ofNullable(getControllerSystemName(ucid)).orElse("N/A"));
            throw new ReportedException(crashReport);
        }
    }

    protected abstract Optional<Controller<?>> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo);

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

    protected void onControllerConnected(Controller<?> controller, boolean hotplug) {
        CUtil.LOGGER.info("Controller connected: {}", ControllerUtils.createControllerString(controller));

        boolean newController = controlify.config().loadOrCreateControllerData(controller);

        ControlifyEvents.CONTROLLER_CONNECTED.invoker().onControllerConnected(controller, hotplug, newController);
    }

    protected void onControllerRemoved(Controller<?> controller) {
        CUtil.LOGGER.info("Controller disconnected: {}", ControllerUtils.createControllerString(controller));

        removeController(controller.uid());

        ControlifyEvents.CONTROLLER_DISCONNECTED.invoker().onControllerDisconnected(controller);
    }

    protected void addController(UniqueControllerID ucid, Controller<?> controller) {
        controllersByUid.put(controller.uid(), controller);
        controllersByJid.put(ucid, controller);
    }

    protected void removeController(String uid) {
        Controller<?> prev = controllersByUid.remove(uid);
        if (prev != null) {
            prev.close();
        }

        Optional.ofNullable(hidInfoByUid.remove(uid))
                .ifPresent(controlify.controllerHIDService()::unconsumeController);
    }

    @Override
    public List<Controller<?>> getConnectedControllers() {
        return ImmutableList.copyOf(controllersByUid.values());
    }

    @Override
    public boolean isControllerConnected(String uid) {
        return controllersByUid.containsKey(uid);
    }

    @Override
    public void close() {
        controllersByUid.values().forEach(Controller::close);
    }

    protected abstract void loadGamepadMappings(Resource resource);

    protected abstract String getControllerSystemName(UniqueControllerID ucid);
}
