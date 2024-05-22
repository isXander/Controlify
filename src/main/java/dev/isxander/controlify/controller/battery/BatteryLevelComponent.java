package dev.isxander.controlify.controller.battery;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class BatteryLevelComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("battery_level");

    private BatteryLevel batteryLevel = BatteryLevel.UNKNOWN;

    public BatteryLevel getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel(BatteryLevel batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
