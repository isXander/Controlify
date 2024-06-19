package dev.isxander.controlify.wireless;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.battery.PowerState;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.utils.ToastUtils;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LowBatteryNotifier {
    private static final Set<String> notifiedControllers = new HashSet<>();
    private static int interval;

    public static void tick() {
        if (interval > 0) {
            interval--;
            return;
        }
        interval = 20 * 60; // 1 minute

        if (!Controlify.instance().config().globalSettings().notifyLowBattery)
            return;

        ControllerManager controllerManager = Controlify.instance().getControllerManager().orElse(null);
        if (controllerManager == null)
            return;

        for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
            PowerState batteryLevel = controller.batteryLevel()
                    .map(BatteryLevelComponent::getBatteryLevel)
                    .orElse(new PowerState.Unknown());

            if (batteryLevel instanceof PowerState.Unknown || batteryLevel instanceof PowerState.WiredOnly) {
                continue;
            }

            String uid = controller.info().uid();
            int percent = batteryLevel.percent();

            if (percent <= 10) {
                if (!notifiedControllers.contains(uid)) {
                    ToastUtils.sendToast(
                            Component.translatable("controlify.toast.low_battery.title"),
                            Component.translatable("controlify.toast.low_battery.message", controller.name()),
                            true
                    );

                    notifiedControllers.add(uid);
                }
            } else {
                notifiedControllers.remove(uid);
            }
        }
    }
}
