package dev.isxander.controlify.utils;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.driver.SDL3NativesManager;
import net.minecraft.SharedConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DebugDump {
    public static void dumpDebug() {
        StringBuilder dump = new StringBuilder();

        dump.append("CONTROLIFY DEBUG DUMP - ");
        LocalDateTime dateTime = LocalDateTime.now();
        String formattedDate = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        dump.append(formattedDate).append("\n\n");

        dump.append("Controlify version: ").append(CUtil.VERSION.getFriendlyString()).append("\n");
        dump.append("Minecraft version: ").append(SharedConstants.getCurrentVersion().getName()).append("\n");

        dumpClientDebug(dump);
    }

    private static void dumpClientDebug(StringBuilder dump) {
        dump.append("SDL3 loaded: ").append(SDL3NativesManager.isLoaded());
        dump.append("Platform: ").append(SDL3NativesManager.Target.CURRENT.formatted());

        Optional<ControllerManager> controllerManagerOpt = Controlify.instance().getControllerManager();
        if (controllerManagerOpt.isPresent()) {
            ControllerManager controllerManager = controllerManagerOpt.get();
            dump.append("Controller-manager: ").append(controllerManager.getClass().getName());

            dump.append("Connected Controllers:");
            for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
                dump.append("");
            }
        }
    }

    private static class IndentedStringBuilder {

    }
}
