package dev.isxander.controlify.driver;

import com.google.common.collect.Sets;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.sdl2.SDL2NativesManager;
import dev.isxander.controlify.debug.DebugProperties;
import org.hid4java.HidDevice;

import java.util.*;

public record GamepadDrivers(BasicGamepadInputDriver basicGamepadInputDriver, GyroDriver gyroDriver, RumbleDriver rumbleDriver, BatteryDriver batteryDriver, GUIDProvider guidProviderDriver, NameProviderDriver nameProviderDriver) {
    public Set<Driver> getUniqueDrivers() {
        Set<Driver> drivers = Sets.newIdentityHashSet();
        drivers.addAll(List.of(basicGamepadInputDriver, gyroDriver, rumbleDriver, batteryDriver));
        return drivers;
    }

    public void printDrivers() {
        if (DebugProperties.PRINT_DRIVER) {
            Controlify.LOGGER.info("Drivers in use: Basic Input = '{}', Gyro = '{}', Rumble = '{}', Battery = '{}', Name = '{}', GUID = '{}'",
                    basicGamepadInputDriver.getBasicGamepadDetails(),
                    gyroDriver.getGyroDetails(),
                    rumbleDriver.getRumbleDetails(),
                    batteryDriver.getBatteryDriverDetails(),
                    nameProviderDriver.getNameProviderDetails(),
                    guidProviderDriver.getGUIDProviderDetails()
            );
        }
    }

    public static GamepadDrivers forController(int jid, Optional<HidDevice> hid) {
        GLFWGamepadDriver glfwDriver = new GLFWGamepadDriver(jid);

        BasicGamepadInputDriver basicGamepadInputDriver = glfwDriver;
        NameProviderDriver nameProviderDriver = glfwDriver;
        GUIDProvider guidProviderDriver = glfwDriver;

        GyroDriver gyroDriver = GyroDriver.UNSUPPORTED;
        RumbleDriver rumbleDriver = RumbleDriver.UNSUPPORTED;
        BatteryDriver batteryDriver = BatteryDriver.UNSUPPORTED;

        if (SDL2NativesManager.isLoaded()) {
            SDL2GamepadDriver sdl2Driver = new SDL2GamepadDriver(jid);
            gyroDriver = sdl2Driver;
            rumbleDriver = sdl2Driver;
            batteryDriver = sdl2Driver;

            // SDL2 bypasses XInput abstraction
            guidProviderDriver = sdl2Driver;
        }

        // TODO: Fix Steam Deck driver
        if (hid.isPresent() && SteamDeckDriver.isSteamDeck(hid.get()) && false) {
            gyroDriver = new SteamDeckDriver(hid.get());
        }

        if (GameControllerDBDriver.isSupported(guidProviderDriver.getGUID())) {
            nameProviderDriver = new GameControllerDBDriver(guidProviderDriver.getGUID());
        }

        return new GamepadDrivers(basicGamepadInputDriver, gyroDriver, rumbleDriver, batteryDriver, guidProviderDriver, nameProviderDriver);
    }
}
