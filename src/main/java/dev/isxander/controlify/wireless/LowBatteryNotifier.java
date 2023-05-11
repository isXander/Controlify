package dev.isxander.controlify.wireless;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.ControllerManager;
import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.utils.ToastUtils;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class LowBatteryNotifier {
    private static final Map<String, BatteryLevel> previousBatteryLevels = new HashMap<>();
    private static int interval;

    public static void tick() {
        if (interval > 0) {
            interval--;
            return;
        }
        interval = 20 * 60; // 1 minute

        if (!Controlify.instance().config().globalSettings().notifyLowBattery)
            return;

        for (Controller<?, ?> controller : ControllerManager.getConnectedControllers()) {
            BatteryLevel batteryLevel = controller.batteryLevel();
            if (batteryLevel == BatteryLevel.UNKNOWN) {
                continue;
            }

            String uid = controller.uid();
            if (previousBatteryLevels.containsKey(uid)) {
                BatteryLevel previousBatteryLevel = previousBatteryLevels.get(uid);
                if (batteryLevel.ordinal() < previousBatteryLevel.ordinal()) {
                    if (batteryLevel == BatteryLevel.LOW) {
                        ToastUtils.sendToast(
                                Component.translatable("controlify.toast.low_battery.title"),
                                Component.translatable("controlify.toast.low_battery.message", controller.name()),
                                true
                        );
                    }
                }
            }

            previousBatteryLevels.put(uid, batteryLevel);
        }
    }
}
