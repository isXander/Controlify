package dev.isxander.controlify.controllermanager;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.DebugLog;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractControllerManager implements ControllerManager {
    protected final Controlify controlify;
    protected final Minecraft minecraft;

    protected final Map<UniqueControllerID, ControllerEntity> controllersByJid = new Object2ObjectOpenHashMap<>();
    protected final Map<String, ControllerEntity> controllersByUid = new Object2ObjectOpenHashMap<>();
    protected final Map<String, ControllerHIDService.ControllerHIDInfo> hidInfoByUid = new Object2ObjectOpenHashMap<>();

    protected final Map<String, Driver> driversByUid = new Object2ObjectOpenHashMap<>();

    public AbstractControllerManager() {
        this.controlify = Controlify.instance();
        this.minecraft = Minecraft.getInstance();

        this.loadGamepadMappings(minecraft.getResourceManager());
    }

    public Optional<ControllerEntity> tryCreate(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo) {
        try {
            if (controllersByJid.containsKey(ucid)) {
                CUtil.LOGGER.warn("Tried to create controller that already is initialised: {}", ucid);
                return Optional.empty();
            }

            if (hidInfo.type().dontLoad()) {
                DebugLog.log("Preventing load of controller #" + ucid + " because its type prevents loading.");
                return Optional.empty();
            }

            return createController(ucid, hidInfo);
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to create controller #{}!", ucid, e);
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

    protected abstract Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo);

    @Override
    public void tick(boolean outOfFocus) {
        for (Driver driver : driversByUid.values()) {
            driver.update(outOfFocus);
            ControlifyEvents.CONTROLLER_STATE_UPDATE.invoke(new ControlifyEvents.ControllerStateUpdate(driver.getController()));
        }
    }

    protected void onControllerConnected(ControllerEntity controller, boolean hotplug) {
        boolean newController = controlify.config().loadControllerConfig(controller);

        CUtil.LOGGER.info("Controller connected: {}", ControllerUtils.createControllerString(controller));

        ControlifyEvents.CONTROLLER_CONNECTED.invoke(new ControlifyEvents.ControllerConnected(controller, hotplug, newController));
    }

    protected void onControllerRemoved(ControllerEntity controller) {
        CUtil.LOGGER.info("Controller disconnected: {}", ControllerUtils.createControllerString(controller));

        removeController(controller.info().uid());

        ControlifyEvents.CONTROLLER_DISCONNECTED.invoke(new ControlifyEvents.ControllerDisconnected(controller));
    }

    @Override
    public Optional<ControllerEntity> reinitController(ControllerEntity controller, ControllerHIDService.ControllerHIDInfo hidInfo) {
        onControllerRemoved(controller);

        Optional<ControllerEntity> newController = tryCreate(controller.info().ucid(), hidInfo);
        newController.ifPresent(c -> {
            ControllerUtils.wrapControllerError(() -> onControllerConnected(c, true), "Connecting controller", c);
        });
        return newController;
    }

    protected void addController(UniqueControllerID ucid, ControllerEntity controller, Driver driver) {
        controllersByUid.put(controller.info().uid(), controller);
        controllersByJid.put(ucid, controller);
        driversByUid.put(controller.info().uid(), driver);
    }

    protected void removeController(String uid) {
        ControllerEntity controller = controllersByUid.remove(uid);
        controllersByJid.remove(controller.info().ucid());
        Optional.ofNullable(hidInfoByUid.remove(uid))
                .ifPresent(controlify.controllerHIDService()::unconsumeController);
        closeController(uid);
        driversByUid.remove(uid);
    }

    @Override
    public void closeController(String uid) {
        driversByUid.get(uid).close();
    }

    @Override
    public List<ControllerEntity> getConnectedControllers() {
        return ImmutableList.copyOf(controllersByUid.values());
    }

    @Override
    public boolean isControllerConnected(String uid) {
        return controllersByUid.containsKey(uid);
    }

    protected int getControllerCountWithMatchingHID(HIDIdentifier hid) {
        return (int) controllersByJid.values().stream()
                .filter(c -> c.info().hid().equals(Optional.ofNullable(hid)))
                .count();
    }

    @Override
    public void close() {
        driversByUid.values().forEach(Driver::close);
    }

    protected abstract void loadGamepadMappings(ResourceProvider resourceProvider);

    protected abstract String getControllerSystemName(UniqueControllerID ucid);
}
