package dev.isxander.controlify.controller.hid;

import com.mojang.datafixers.util.Pair;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerType;
import org.hid4java.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ControllerHIDService {
    private final HidServicesSpecification specification;
    private HidServices services;

    private final Queue<Pair<HidDevice, HIDIdentifier>> unconsumedControllerHIDs;
    private final Map<String, HidDevice> attachedDevices = new HashMap<>();
    private boolean disabled = false;
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
            Controlify.LOGGER.error("Failed to start controller HID service! If you are on Linux using flatpak or snap, this is likely because your launcher has not added libusb to their package.", e);
            disabled = true;
        }
    }

    public ControllerHIDInfo fetchType() {
        if (disabled) {
            return new ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty());
        }

        doScanOnThisThread();

        Pair<HidDevice, HIDIdentifier> hid = unconsumedControllerHIDs.poll();
        if (hid == null) {
            Controlify.LOGGER.warn("No controller found via USB hardware scan! This prevents identifying controller type.");
            return new ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty());
        }

        ControllerType type = ControllerType.getTypeMap().getOrDefault(hid.getSecond(), ControllerType.UNKNOWN);
        if (type == ControllerType.UNKNOWN)
            Controlify.LOGGER.warn("Controller found via USB hardware scan, but it was not found in the controller identification database! (HID: {})", hid.getSecond());

        unconsumedControllerHIDs.removeIf(h -> hid.getFirst().getPath().equals(h.getFirst().getPath()));

        return new ControllerHIDInfo(type, Optional.of(hid.getFirst()));
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

    public void unconsumeController(ControllerHIDInfo hid) {
        hid.hidDevice.ifPresent(device -> attachedDevices.remove(device.getPath()));
    }

    private boolean isController(HidDevice device) {
        boolean isControllerType = ControllerType.getTypeMap().containsKey(new HIDIdentifier(device.getVendorId(), device.getProductId()));
        boolean isGenericDesktopControlOrGameControl = device.getUsagePage() == 0x1 || device.getUsagePage() == 0x5;
        boolean isSelfIdentifiedController = CONTROLLER_USAGE_IDS.contains(device.getUsage());

        return isControllerType || (isGenericDesktopControlOrGameControl && isSelfIdentifiedController);
    }

    public record ControllerHIDInfo(ControllerType type, Optional<HidDevice> hidDevice) {
        public Optional<String> createControllerUID() {
            return hidDevice.map(HidDevice::getPath).map(p -> UUID.nameUUIDFromBytes(p.getBytes())).map(UUID::toString);
        }
    }
}
