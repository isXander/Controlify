package dev.isxander.controlify.utils;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.driver.SDL3NativesManager;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DebugDump {
    public static String dumpDebug() {
        IndentedStringBuilder dump = new IndentedStringBuilder();

        LocalDateTime dateTime = LocalDateTime.now();
        String formattedDate = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        dump.line("CONTROLIFY DEBUG DUMP - ", formattedDate, '\n');

        dump.line("Controlify version: ", PlatformMainUtil.getControlifyVersion());
        dump.line("Minecraft version: ", SharedConstants.getCurrentVersion().getName());

        if (PlatformMainUtil.getEnv() == Environment.CLIENT) {
            dump.line("Client").pushIndent();
            dumpClientDebug(dump);
            dump.popIndent();
        }

        return dump.build();
    }

    private static void dumpClientDebug(IndentedStringBuilder dump) {
        dump.line("SDL3 loaded: ", SDL3NativesManager.isLoaded());
        dump.line("Platform: ", SDL3NativesManager.Target.CURRENT.formatted());
        dump.line();

        Optional<ControllerManager> controllerManagerOpt = Controlify.instance().getControllerManager();
        if (controllerManagerOpt.isPresent()) {
            ControllerManager controllerManager = controllerManagerOpt.get();
            dump.line("Controller-manager: ", controllerManager.getClass().getName());

            dump.line("Connected Controllers:").pushIndent();
            for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
                dump.line("Name: ", controller.name());
                dump.line("Identified type: ", controller.info().type());
                dump.line("GUID: ", controller.info().guid());
                dump.line("UID: ", controller.info().uid());
                dump.line("UCID: ", controller.info().ucid());
                controller.info().hid().ifPresent(hid -> dump.line("HID: ", hid.asIdentifier()));

                controller.input().ifPresentOrElse(input -> {
                    dump.line("Input Component:").pushIndent();

                    dump.line("Definitely gamepad: ", input.isDefinitelyGamepad());
                    if (!input.isDefinitelyGamepad()) {
                        dump.line("Available inputs:").pushIndent();
                        for (ResourceLocation button : input.stateNow().getButtons()) {
                            dump.line("BTN ", button);
                        }
                        for (ResourceLocation axis : input.stateNow().getAxes()) {
                            dump.line("AXS ", axis);
                        }
                        for (ResourceLocation hat : input.stateNow().getHats()) {
                            dump.line("HAT ", hat);
                        }
                        dump.popIndent(); // available inputs
                    }

                    dump.popIndent(); // input component
                }, () -> dump.line("Input Component: UNSUPPORTED"));

                dump.line("Rumble supported: ", controller.rumble().isPresent());
                dump.line("Trigger rumble supported: ", controller.triggerRumble().isPresent());
                dump.line("Battery level: ", controller.batteryLevel().map(b -> b.getBatteryLevel().toString()).orElse("UNSUPPORTED"));
                dump.line("Gyro supported: ", controller.gyro().isPresent());
                controller.touchpad().ifPresentOrElse(touchpad -> {
                    dump.line("Touchpads max fingers: ", touchpad.getMaxFingers());
                }, () -> dump.line("Touchpads: UNSUPPORTED"));
                dump.line("HD haptics supported: ", controller.hdHaptics().isPresent());

                dump.line();
            }
            dump.popIndent(); // connected controllers
        }
    }

    private static class IndentedStringBuilder {
        private final StringBuilder sb = new StringBuilder();
        private int indent;

        public IndentedStringBuilder line(Object... parts) {
            sb.append("  ".repeat(indent));
            for (Object part : parts) {
                sb.append(part);
            }
            sb.append('\n');

            return this;
        }

        public void pushIndent() {
            indent++;
        }

        public void popIndent() {
            indent--;
        }

        public String build() {
            return sb.toString();
        }
    }
}
