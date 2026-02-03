package dev.isxander.controlify.controller.misc;

import dev.isxander.controlify.config.settings.profile.BluetoothDeviceSettings;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class BluetoothDeviceComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("bluetooth");

    public BluetoothDeviceSettings settings() {
        return this.controller().settings().bluetoothDevice;
    }

    public BluetoothDeviceSettings defaultSettings() {
        return this.controller().defaultSettings().bluetoothDevice;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
