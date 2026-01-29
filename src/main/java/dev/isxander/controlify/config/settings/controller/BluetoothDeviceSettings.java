package dev.isxander.controlify.config.settings.controller;

import dev.isxander.controlify.config.dto.controller.BluetoothDeviceConfig;

public class BluetoothDeviceSettings {
    public boolean dontShowWarning;

    public BluetoothDeviceSettings(boolean dontShowWarning) {
        this.dontShowWarning = dontShowWarning;
    }

    public static BluetoothDeviceSettings fromDTO(BluetoothDeviceConfig dto) {
        return new BluetoothDeviceSettings(dto.dontShowWarning());
    }

    public BluetoothDeviceConfig toDTO() {
        return new BluetoothDeviceConfig(dontShowWarning);
    }
}
