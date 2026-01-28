package dev.isxander.controlify.config3.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BluetoothDeviceConfig(
        boolean dontShowWarning
) {
    public static final Codec<BluetoothDeviceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("dont_show_warning").forGetter(BluetoothDeviceConfig::dontShowWarning)
    ).apply(instance, BluetoothDeviceConfig::new));
}
