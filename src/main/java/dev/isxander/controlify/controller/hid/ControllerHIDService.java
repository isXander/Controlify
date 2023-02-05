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

    public ControllerHIDService() {
        this.deviceQueue = new ArrayDeque<>();

        this.specification = new HidServicesSpecification();
        specification.setAutoStart(false);
        specification.setScanInterval(2000); // long interval, so we can guarantee this runs after GLFW hook
    }

    public void start() {
        var services = HidManager.getHidServices(specification);
        services.addHidServicesListener(this);

        services.start();
    }

    public void awaitNextController(Consumer<HidDevice> consumer) {
        deviceQueue.add(consumer);
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        var device = event.getHidDevice();

        if (isController(device)) {
            if (deviceQueue.peek() != null) {
                deviceQueue.poll().accept(event.getHidDevice());
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

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {

    }

    @Override
    public void hidFailure(HidServicesEvent event) {

    }
}
