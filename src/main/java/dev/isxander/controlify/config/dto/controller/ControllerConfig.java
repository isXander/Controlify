package dev.isxander.controlify.config.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ControllerConfig(
        GenericControllerConfig generic,
        InputConfig input,
        RumbleConfig rumble,
        HDHapticConfig hdHaptic,
        GyroConfig gyro,
        BluetoothDeviceConfig bluetoothDevice
) {
    public static final Codec<ControllerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GenericControllerConfig.CODEC.fieldOf("generic").forGetter(ControllerConfig::generic),
            InputConfig.CODEC.fieldOf("input").forGetter(ControllerConfig::input),
            RumbleConfig.CODEC.fieldOf("rumble").forGetter(ControllerConfig::rumble),
            HDHapticConfig.CODEC.fieldOf("hd_haptic").forGetter(ControllerConfig::hdHaptic),
            GyroConfig.CODEC.fieldOf("gyro").forGetter(ControllerConfig::gyro),
            BluetoothDeviceConfig.CODEC.fieldOf("bluetooth_device").forGetter(ControllerConfig::bluetoothDevice)
    ).apply(instance, ControllerConfig::new));
}
