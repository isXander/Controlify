package dev.isxander.controlify.driver.joystick;

import com.google.common.collect.Sets;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.*;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.utils.CUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record JoystickDrivers(
        BasicJoystickInputDriver basicJoystickInputDriver,
        RumbleDriver rumbleDriver,
        BatteryDriver batteryDriver,
        GUIDProvider guidProviderDriver,
        NameProviderDriver nameProviderDriver
) {
    public Set<Driver> getUniqueDrivers() {
        Set<Driver> drivers = Sets.newIdentityHashSet();
        drivers.addAll(List.of(basicJoystickInputDriver, rumbleDriver, batteryDriver, guidProviderDriver, nameProviderDriver));
        return drivers;
    }

    public void printDrivers() {
        if (DebugProperties.PRINT_DRIVER) {
            CUtil.LOGGER.info("Drivers in use: Basic Input = '{}', Rumble = '{}', Battery = '{}', Name = '{}', GUID = '{}'",
                    basicJoystickInputDriver.getBasicJoystickDetails(),
                    rumbleDriver.getRumbleDetails(),
                    batteryDriver.getBatteryDriverDetails(),
                    nameProviderDriver.getNameProviderDetails(),
                    guidProviderDriver.getGUIDProviderDetails()
            );
        }
    }

    public static JoystickDrivers forController(int jid, Optional<HIDDevice> hid) {
        GLFWJoystickDriver glfwDriver = new GLFWJoystickDriver(jid);

        BasicJoystickInputDriver basicJoystickInputDriver = glfwDriver;
        NameProviderDriver nameProviderDriver = glfwDriver;
        GUIDProvider guidProviderDriver = glfwDriver;

        RumbleDriver rumbleDriver = RumbleDriver.UNSUPPORTED;
        BatteryDriver batteryDriver = BatteryDriver.UNSUPPORTED;

        if (SDL2NativesManager.isLoaded()) {
            SDL2JoystickDriver sdl2Driver = new SDL2JoystickDriver(jid);
            basicJoystickInputDriver = sdl2Driver;
            rumbleDriver = sdl2Driver;
            batteryDriver = sdl2Driver;
            guidProviderDriver = sdl2Driver;
        }

        if (GameControllerDBDriver.isSupported(guidProviderDriver.getGUID())) {
            nameProviderDriver = new GameControllerDBDriver(guidProviderDriver.getGUID());
        }

        return new JoystickDrivers(
                basicJoystickInputDriver,
                rumbleDriver,
                batteryDriver,
                guidProviderDriver,
                nameProviderDriver
        );
    }
}
