package dev.isxander.controlify.hid;

import com.mojang.datafixers.util.Pair;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.sdl2.SDL2NativesManager;
import dev.isxander.controlify.utils.Log;
import dev.isxander.controlify.utils.ToastUtils;
import net.minecraft.network.chat.Component;
import org.hid4java.*;
import org.libsdl.SDL;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ControllerHIDService {
    private final HidServicesSpecification specification;
    private HidServices services;

    private final Queue<Pair<HidDevice, HIDIdentifier>> unconsumedControllerHIDs;
    private final Map<String, HidDevice> attachedDevices = new HashMap<>();
    private boolean disabled = false;
    private boolean firstFetch = true;
    // https://learn.microsoft.com/en-us/windows-hardware/drivers/hid/hid-usages#usage-page
    private static final Set<Integer> CONTROLLER_USAGE_IDS = Set.of(
            0x04, // Joystick
            0x05, // Gamepad
            0x08  // Multi-axis Controller
    );

    public ControllerHIDService() {
        this.specification = new HidServicesSpecification();
        specification.setAutoStart(false);
        specification.setScanMode(ScanMode.NO_SCAN);
        this.unconsumedControllerHIDs = new ArrayBlockingQueue<>(50);
    }

    public void start() {
        try {
            services = HidManager.getHidServices(specification);
            services.start();
        } catch (HidException e) {
            Log.LOGGER.error("Failed to start controller HID service! If you are on Linux using flatpak or snap, this is likely because your launcher has not added libusb to their package.", e);
            disabled = true;
        }
    }

    public ControllerHIDInfo fetchType(int jid) {
        if (firstFetch) {
            firstFetch = false;
            if (isDisabled() && !SDL2NativesManager.isLoaded()) {
                if (Controlify.instance().controllerHIDService().isDisabled() && !SDL2NativesManager.isLoaded()) {
                    ToastUtils.sendToast(
                            Component.translatable("controlify.error.hid"),
                            Component.translatable("controlify.error.hid.desc"),
                            true
                    );
                }
            }
        }

        if (disabled) {
            return fetchTypeFromSDL(jid)
                    .orElse(new ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty()));
        }

        doScanOnThisThread();

        Pair<HidDevice, HIDIdentifier> hid = unconsumedControllerHIDs.poll();
        if (hid == null) {
            Log.LOGGER.warn("No controller found via USB hardware scan! Using SDL if available.");

            return fetchTypeFromSDL(jid)
                    .orElse(new ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty()));
        }

        ControllerType type = ControllerType.getTypeForHID(hid.getSecond());

        unconsumedControllerHIDs.removeIf(h -> hid.getFirst().getPath().equals(h.getFirst().getPath()));

        return new ControllerHIDInfo(type, Optional.of(new HIDDevice.Hid4Java(hid.getFirst())));
    }

    public boolean isDisabled() {
        return disabled;
    }

    private void doScanOnThisThread() {
        List<String> removeList = new ArrayList<String>();

        List<HidDevice> attachedHidDeviceList = services.getAttachedHidDevices();

        for (HidDevice attachedDevice : attachedHidDeviceList) {

            if (!this.attachedDevices.containsKey(attachedDevice.getId())) {

                // Device has become attached so add it but do not open
                attachedDevices.put(attachedDevice.getId(), attachedDevice);

                // add an unconsumed identifier that can be removed if not disconnected
                HIDIdentifier identifier = new HIDIdentifier(attachedDevice.getVendorId(), attachedDevice.getProductId());
                if (isController(attachedDevice))
                    unconsumedControllerHIDs.add(new Pair<>(attachedDevice, identifier));
            }
        }

        for (Map.Entry<String, HidDevice> entry : attachedDevices.entrySet()) {

            String deviceId = entry.getKey();
            HidDevice hidDevice = entry.getValue();

            if (!attachedHidDeviceList.contains(hidDevice)) {

                // Keep track of removals
                removeList.add(deviceId);

                // remove device from unconsumed list
                unconsumedControllerHIDs.removeIf(device -> this.attachedDevices.get(deviceId).getPath().equals(device.getFirst().getPath()));
            }
        }

        if (!removeList.isEmpty()) {
            // Update the attached devices map
            removeList.forEach(this.attachedDevices.keySet()::remove);
        }
    }

    private Optional<ControllerHIDInfo> fetchTypeFromSDL(int jid) {
        if (SDL2NativesManager.isLoaded()) {
            int vid = SDL.SDL_JoystickGetDeviceVendor(jid);
            int pid = SDL.SDL_JoystickGetDeviceProduct(jid);
            String path = GLFW.glfwGetJoystickGUID(jid);

            if (vid != 0 && pid != 0) {
                Log.LOGGER.info("Using SDL to identify controller type.");
                return Optional.of(new ControllerHIDInfo(
                        ControllerType.getTypeForHID(new HIDIdentifier(vid, pid)),
                        Optional.of(new HIDDevice.IDOnly(vid, pid, path))
                ));
            }
        }
        return Optional.empty();
    }

    public void unconsumeController(ControllerHIDInfo hid) {
        hid.hidDevice.ifPresent(device -> attachedDevices.remove(device.path()));
    }

    private boolean isController(HidDevice device) {
        boolean isControllerType = ControllerType.getTypeMap().containsKey(new HIDIdentifier(device.getVendorId(), device.getProductId()));
        boolean isGenericDesktopControlOrGameControl = device.getUsagePage() == 0x1 || device.getUsagePage() == 0x5;
        boolean isSelfIdentifiedController = CONTROLLER_USAGE_IDS.contains(device.getUsage());

        return isControllerType || (isGenericDesktopControlOrGameControl && isSelfIdentifiedController);
    }

    public record ControllerHIDInfo(ControllerType type, Optional<HIDDevice> hidDevice) {
        public Optional<String> createControllerUID() {
            return hidDevice.map(HIDDevice::path).map(p -> UUID.nameUUIDFromBytes(p.getBytes())).map(UUID::toString);
        }
    }
}
