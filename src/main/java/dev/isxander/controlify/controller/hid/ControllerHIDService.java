package dev.isxander.controlify.controller.hid;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerType;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class ControllerHIDService implements HidServicesListener {
    // https://learn.microsoft.com/en-us/windows-hardware/drivers/hid/hid-usages#usage-page
    private static final Set<Integer> CONTROLLER_USAGE_IDS = Set.of(
            0x04, // Joystick
            0x05, // Gamepad
            0x08  // Multi-axis Controller
    );

    private final HidServicesSpecification specification;
    private final Queue<Consumer<HidDevice>> deviceQueue;

    private boolean disabled = false;

    public ControllerHIDService() {
        this.deviceQueue = new ArrayDeque<>();

        this.specification = new HidServicesSpecification();
        specification.setAutoStart(false);
        specification.setScanInterval(2000); // long interval, so we can guarantee this runs after GLFW hook
    }

    public void start() {
        try {
            var services = HidManager.getHidServices(specification);
            services.addHidServicesListener(this);

            services.start();
        } catch (HidException e) {
            Controlify.LOGGER.error("Failed to start controller HID service! If you are on Linux using flatpak or snap, this is likely because your launcher has not added libusb to their package.", e);
            disabled = true;
        }
    }

    public void awaitNextController(Consumer<HidDevice> consumer) {
        if (disabled) {
            consumer.accept(null);
            return;
        }
        deviceQueue.add(consumer);
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        var device = event.getHidDevice();

        if (isController(device)) {
            if (deviceQueue.peek() != null) {
                deviceQueue.poll().accept(device);
            } else {
                Controlify.LOGGER.error("Unhandled controller: " + ControllerType.getTypeForHID(new HIDIdentifier(device.getVendorId(), device.getProductId())).friendlyName());
            }
        }
    }

    private boolean isController(HidDevice device) {
        var isGenericDesktopControlOrGameControl = device.getUsagePage() == 0x1 || device.getUsagePage() == 0x5;
        var isController = CONTROLLER_USAGE_IDS.contains(device.getUsage());
        return isGenericDesktopControlOrGameControl && isController;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {

    }

    @Override
    public void hidFailure(HidServicesEvent event) {

    }
}
