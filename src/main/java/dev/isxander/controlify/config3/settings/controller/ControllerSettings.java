package dev.isxander.controlify.config3.settings.controller;

import dev.isxander.controlify.config3.dto.controller.ControllerConfig;

public class ControllerSettings {
    public InputSettings input;
    public RumbleSettings rumble;
    public HDHapticSettings hdHaptic;
    public GyroSettings gyro;
    public BluetoothDeviceSettings bluetoothDevice;

    public ControllerSettings(
            InputSettings input,
            RumbleSettings rumble,
            HDHapticSettings hdHaptic,
            GyroSettings gyro,
            BluetoothDeviceSettings bluetoothDevice
    ) {
        this.input = input;
        this.rumble = rumble;
        this.hdHaptic = hdHaptic;
        this.gyro = gyro;
        this.bluetoothDevice = bluetoothDevice;
    }

    public static ControllerSettings fromDTO(ControllerConfig dto) {
        return new ControllerSettings(
                InputSettings.fromDTO(dto.input()),
                RumbleSettings.fromDTO(dto.rumble()),
                HDHapticSettings.fromDTO(dto.hdHaptic()),
                GyroSettings.fromDTO(dto.gyro()),
                BluetoothDeviceSettings.fromDTO(dto.bluetoothDevice())
        );
    }

    public ControllerConfig toDTO() {
        return new ControllerConfig(
                input.toDTO(),
                rumble.toDTO(),
                hdHaptic.toDTO(),
                gyro.toDTO(),
                bluetoothDevice.toDTO()
        );
    }
}
