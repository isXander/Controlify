package dev.isxander.controlify.controllermanager;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractControllerManager implements ControllerManager {
    protected final Controlify controlify;
    protected final Minecraft minecraft;

    protected final Map<UniqueControllerID, ControllerEntity> controllersByJid = new Object2ObjectOpenHashMap<>();
    protected final Map<ControllerUID, ControllerEntity> controllersByUid = new Object2ObjectOpenHashMap<>();
    protected final Map<ControllerUID, ControllerHIDService.ControllerHIDInfo> hidInfoByUid = new Object2ObjectOpenHashMap<>();

    protected final ControlifyLogger logger;

    public AbstractControllerManager(ControlifyLogger logger) {
        this.controlify = Controlify.instance();
        this.minecraft = Minecraft.getInstance();
        this.logger = logger.createSubLogger("ControllerManager");

        this.loadGamepadMappings(minecraft.getResourceManager());
    }

    public Optional<ControllerEntity> tryCreate(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo) {
        ControlifyLogger controllerLogger = logger.createSubLogger("Controller #" + ucid);

        try {
            if (controllersByJid.containsKey(ucid)) {
                controllerLogger.warn("Tried to create controller that already is initialised: {}.", ucid);
                return Optional.empty();
            }

            if (hidInfo.type().dontLoad()) {
                controllerLogger.debugLog("Preventing load of controller #" + ucid + " because its type prevents loading.");
                return Optional.empty();
            }

            if (hidInfo.type().isSteamDeck()
                && SteamDeckUtil.DECK_MODE.isDesktopMode()
            ) {
                controllerLogger.log("Preventing load of controller #{} because Steam Deck is in desktop mode.", ucid);
                return Optional.empty();
            }

            return createController(ucid, hidInfo, controllerLogger);
        } catch (Throwable e) {
            controllerLogger.error("Failed to create controller #{}!", e, ucid);
            CrashReport crashReport = CrashReport.forThrowable(e, "Creating controller #" + ucid);
            CrashReportCategory category = crashReport.addCategory("Controller Info");
            category.setDetail("Unique controller ID", ucid);
            category.setDetail("Controller identification", hidInfo.type());
            category.setDetail("HID path", hidInfo.hidDevice().map(HIDDevice::path).orElse("N/A"));
            category.setDetail("HID service status", Controlify.instance().controllerHIDService().isDisabled() ? "Disabled" : "Enabled");
            category.setDetail("System name", Optional.ofNullable(getControllerSystemName(ucid)).orElse("N/A"));
            controllerLogger.crashReport(crashReport);
            return Optional.empty();
            //throw new ReportedException(crashReport);
        }
    }

    protected abstract Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo, ControlifyLogger controllerLogger);

    @Override
    public void tick(boolean outOfFocus) {
        for (ControllerEntity controller : controllersByUid.values()) {
            controller.update(outOfFocus);
            ControlifyEvents.CONTROLLER_STATE_UPDATE.invoke(new ControlifyEvents.ControllerStateUpdate(controller));
        }
    }

    protected void onControllerConnected(ControllerEntity controller, boolean hotplug) {
        boolean newController = controlify.config().loadControllerConfig(controller);

        logger.log("Controller connected: {}", ControllerUtils.createControllerString(controller));

        this.controlify.onControllerAdded(controller, hotplug, newController);
    }

    protected void onControllerRemoved(ControllerEntity controller) {
        logger.log("Controller disconnected: {}", ControllerUtils.createControllerString(controller));

        closeController(controller.uid());

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

    protected void addController(UniqueControllerID ucid, ControllerEntity controller) {
        controllersByUid.put(controller.uid(), controller);
        controllersByJid.put(ucid, controller);
    }

    @Override
    public void closeController(ControllerUID uid) {
        ControllerEntity controller = controllersByUid.remove(uid);

        if (controller == null) return;

        controller.close();

        controllersByJid.remove(controller.info().ucid());
        Optional.ofNullable(hidInfoByUid.remove(uid))
                .ifPresent(controlify.controllerHIDService()::unconsumeController);
    }

    @Override
    public List<ControllerEntity> getConnectedControllers() {
        return ImmutableList.copyOf(controllersByUid.values());
    }

    @Override
    public boolean isControllerConnected(ControllerUID uid) {
        return controllersByUid.containsKey(uid);
    }

    protected int getControllerCountWithMatchingHID(HIDIdentifier hid) {
        return (int) controllersByJid.values().stream()
                .filter(c -> c.info().hid().isPresent() && c.info().hid().get().asIdentifier().equals(hid))
                .count();
    }

    @Override
    public void close() {
        controllersByUid.values().forEach(ControllerEntity::close);
    }

    protected abstract void loadGamepadMappings(ResourceProvider resourceProvider);

    protected abstract String getControllerSystemName(UniqueControllerID ucid);
}
