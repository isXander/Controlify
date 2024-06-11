package dev.isxander.controlify.controller.battery;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class BatteryLevelComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("battery_level");

    private PowerState batteryLevel = new PowerState.Unknown();

    public PowerState getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel(PowerState batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
