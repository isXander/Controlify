package dev.isxander.controlify.hid;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.util.Pair;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.driver.SDL3NativesManager;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ToastUtils;
import net.minecraft.network.chat.Component;
import org.hid4java.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        if (SDL3NativesManager.Target.CURRENT.isMacArm()) {
            CUtil.LOGGER.warn("HID support is not available on Apple Silicon Macs. SDL will be used instead.");
            disabled = true;
            return;
        }

        try {
            services = HidManager.getHidServices(specification);
            services.start();
        } catch (HidException e) {
            CUtil.LOGGER.error("Failed to start controller HID service! If you are on Linux using flatpak or snap, this is likely because your launcher has not added libusb to their package.", e);
            disabled = true;
        }
    }

    public void stop() {
        if (!disabled && services != null) {
            services.shutdown();
            disabled = true;
        }
    }

    public ControllerHIDInfo fetchType(int jid) {
        ControllerHIDInfo info;
        try {
            info = fetchType0(jid);
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to fetch controller type!", e);
            info = new ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty());
        }

        if (DebugProperties.PRINT_VID_PID) {
            info.hidDevice.ifPresent(hid -> {
                var hex = HexFormat.of().withPrefix("0x");
                CUtil.LOGGER.info("VID: {}, PID: {}", hex.toHexDigits(hid.vendorId()), hex.toHexDigits(hid.productId()));
            });
        }

        return info;
    }

    private ControllerHIDInfo fetchType0(int jid) {
        if (firstFetch) {
            firstFetch = false;
            if (isDisabled() && !SDL3NativesManager.isLoaded()) {
                if (Controlify.instance().controllerHIDService().isDisabled() && !SDL3NativesManager.isLoaded()) {
                    ToastUtils.sendToast(
                            Component.translatable("controlify.error.hid"),
                            Component.translatable("controlify.error.hid.desc"),
                            true
                    );
                }
            }
        }

        if (disabled) {
            return new ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty());
        }

        doScanOnThisThread();

        Pair<HidDevice, HIDIdentifier> hid = unconsumedControllerHIDs.poll();
        if (hid == null) {
            CUtil.LOGGER.warn("No controller found via USB hardware scan! Using SDL if available.");

            return new ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty());
        }

        ControllerType type = Controlify.instance().controllerTypeManager().getControllerType(hid.getSecond());

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

    public void unconsumeController(ControllerHIDInfo hid) {
        hid.hidDevice.ifPresent(device -> attachedDevices.remove(device.path()));
    }

    private boolean isController(HidDevice device) {
        boolean isControllerType = Controlify.instance().controllerTypeManager().getTypeMap()
                .containsKey(new HIDIdentifier(device.getVendorId(), device.getProductId()));
        boolean isGenericDesktopControlOrGameControl = device.getUsagePage() == 0x1 || device.getUsagePage() == 0x5;
        boolean isSelfIdentifiedController = CONTROLLER_USAGE_IDS.contains(device.getUsage());

        return isControllerType || (isGenericDesktopControlOrGameControl && isSelfIdentifiedController);
    }

    public record ControllerHIDInfo(ControllerType type, Optional<HIDDevice> hidDevice) {
        public Optional<String> createControllerUID(int controllerIndex) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            md.update(Ints.toByteArray(controllerIndex));
            hidDevice.ifPresent(hid -> {
                md.update(Ints.toByteArray(hid.vendorId()));
                md.update(Ints.toByteArray(hid.productId()));
            });

            String namespace = type().namespace().toString();
            if ("controlify".equals(type().namespace().getNamespace())) {
                // maintains backwards compatibility
                namespace = type().namespace().getPath();
            }

            md.update(namespace.getBytes());

            return Optional.of(UUID.nameUUIDFromBytes(md.digest()).toString());
        }
    }
}
