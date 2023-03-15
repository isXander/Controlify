package dev.isxander.controlify.controller.hid;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerType;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import java.util.*;

public class ControllerHIDService implements HidServicesListener {
    private final HidServicesSpecification specification;
    private HidServices services;

    private final Map<String, HIDIdentifier> unconsumedHIDs;
    private boolean disabled = false;

    public ControllerHIDService() {
        this.specification = new HidServicesSpecification();
        specification.setAutoStart(false);
        this.unconsumedHIDs = new LinkedHashMap<>();
    }

    public void start() {
        try {
            services = HidManager.getHidServices(specification);
            services.addHidServicesListener(this);

            services.start();
        } catch (HidException e) {
            Controlify.LOGGER.error("Failed to start controller HID service! If you are on Linux using flatpak or snap, this is likely because your launcher has not added libusb to their package.", e);
            disabled = true;
        }
    }

    public ControllerHIDInfo fetchType() {
        services.scan();
        try {
            // wait for scan to complete on separate thread
            Thread.sleep(800);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var typeMap = ControllerType.getTypeMap();
        for (var entry : unconsumedHIDs.entrySet()) {
            var path = entry.getKey();
            var hid = entry.getValue();
            var type = typeMap.get(hid);
            if (type != null) {
                Controlify.LOGGER.info("identified controller type " + type);
                unconsumedHIDs.remove(path);
                return new ControllerHIDInfo(type, Optional.of(path));
            }
        }

        Controlify.LOGGER.warn("Controller type unknown! Please report the make and model of your controller and give the following details: " + unconsumedHIDs);
        return new ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty());
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        var device = event.getHidDevice();
        unconsumedHIDs.put(device.getPath(), new HIDIdentifier(device.getVendorId(), device.getProductId()));
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        unconsumedHIDs.remove(event.getHidDevice().getPath());
    }

    @Override
    public void hidFailure(HidServicesEvent event) {

    }

    public record ControllerHIDInfo(ControllerType type, Optional<String> path) {
    }
}
