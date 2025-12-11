package dev.isxander.controlify.controller.battery;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class BatteryLevelComponent implements ECSComponent {
    public static final Identifier ID = CUtil.rl("battery_level");

    private PowerState batteryLevel = new PowerState.Unknown();

    public PowerState getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel(PowerState batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
