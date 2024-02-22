package dev.isxander.controlify.controller;

import dev.isxander.controlify.Controlify;
import net.minecraft.resources.ResourceLocation;

public class BatteryLevelComponent implements ECSComponent {
    public static final ResourceLocation ID = Controlify.id("battery_level");

    private BatteryLevel batteryLevel = BatteryLevel.UNKNOWN;

    public BatteryLevel getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel(BatteryLevel batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
