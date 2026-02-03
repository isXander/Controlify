package dev.isxander.controlify.controller;

import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.dualsense.DualSenseComponent;
import dev.isxander.controlify.controller.haptic.HDHapticComponent;
import dev.isxander.controlify.controller.info.ControllerInfo;
import dev.isxander.controlify.controller.info.DriverNameComponent;
import dev.isxander.controlify.controller.info.GUIDComponent;
import dev.isxander.controlify.controller.info.UIDComponent;
import dev.isxander.controlify.controller.led.LEDComponent;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.impl.ECSEntityImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

public class ControllerEntity extends ECSEntityImpl {
    private final ControllerInfo info;
    private final Driver driver;
    private final ProfileSettings settings;
    private final ProfileSettings defaultSettings;
    private final ControlifyLogger logger;

    public ControllerEntity(
            ControllerInfo info,
            Driver driver,
            ProfileSettings settings,
            ProfileSettings defaultSettings,
            ControlifyLogger logger
    ) {
        this.info = info;
        this.driver = driver;
        this.settings = settings;
        this.defaultSettings = defaultSettings;
        this.logger = logger;

        driver.addComponents(this);
        this.getAllComponents().values().forEach(c -> c.attach(this));

        logger.debugLog("Components: {}", this.getAllComponents().keySet().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
    }

    public ControllerUID uid() {
        return this.<UIDComponent>getComponent(UIDComponent.ID).orElseThrow().value();
    }

    @NotNull
    public String driverName() {
        return this.<DriverNameComponent>getComponent(DriverNameComponent.ID).orElseThrow().value();
    }

    @NotNull
    public String guid() {
        return this.<GUIDComponent>getComponent(GUIDComponent.ID).orElseThrow().value();
    }

    public ControllerInfo info() {
        return this.info;
    }

    @NotNull
    public String name() {
        String friendlyName = info().type().friendlyName();
        if (friendlyName != null)
            return friendlyName;

        return driverName();
    }

    public Driver drivers() {
        return driver;
    }

    public ProfileSettings settings() {
        return settings;
    }

    public ProfileSettings defaultSettings() {
        return defaultSettings;
    }

    @Contract(pure = true)
    public Optional<InputComponent> input() {
        return this.getComponent(InputComponent.ID);
    }

    @Contract(pure = true)
    public Optional<RumbleComponent> rumble() {
        return this.getComponent(RumbleComponent.ID);
    }

    @Contract(pure = true)
    public Optional<TriggerRumbleComponent> triggerRumble() {
        return this.getComponent(TriggerRumbleComponent.ID);
    }

    @Contract(pure = true)
    public Optional<GyroComponent> gyro() {
        return this.getComponent(GyroComponent.ID);
    }

    @Contract(pure = true)
    public Optional<TouchpadComponent> touchpad() {
        return this.getComponent(TouchpadComponent.ID);
    }

    @Contract(pure = true)
    public Optional<BatteryLevelComponent> batteryLevel() {
        return this.getComponent(BatteryLevelComponent.ID);
    }

    @Contract(pure = true)
    public Optional<HDHapticComponent> hdHaptics() {
        return this.getComponent(HDHapticComponent.ID);
    }

    @Contract(pure = true)
    public Optional<DualSenseComponent> dualSense() {
        return this.getComponent(DualSenseComponent.ID);
    }

    @Contract(pure = true)
    public Optional<LEDComponent> led() {
        return this.getComponent(LEDComponent.ID);
    }

    @Contract(pure = true)
    public Optional<BluetoothDeviceComponent> bluetooth() {
        return this.getComponent(BluetoothDeviceComponent.ID);
    }

    public void update(boolean outOfFocus) {
        this.driver.update(this, outOfFocus);
    }

    public void close() {
        this.driver.close();
    }

    public ControlifyLogger getLogger() {
        return logger;
    }
}
