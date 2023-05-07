package dev.isxander.controlify.debug;

import dev.isxander.controlify.Controlify;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;

public class DebugProperties {
    private static final List<DebugProperty> properties = new ArrayList<>();

    public static final boolean DEBUG_LOGGING = boolProp("controlify.debug.logging", false, true);
    /* Renders debug overlay for vmouse snapping */
    public static final boolean DEBUG_SNAPPING = boolProp("controlify.debug.snapping", false, false);
    /* Forces all gamepads to be treated as a regular joystick */
    public static final boolean FORCE_JOYSTICK = boolProp("controlify.debug.force_joystick", false, false);
    /* Prints joystick input counts for making joystick mappings */
    public static final boolean PRINT_JOY_STATE = boolProp("controlify.debug.print_joy_state", false, false);
    /* Print gyro data if supported */
    public static final boolean PRINT_GYRO = boolProp("controlify.debug.print_gyro", false, false);
    /* Print what drivers are being used */
    public static final boolean PRINT_DRIVER = boolProp("controlify.debug.print_driver", true, true);

    public static void printProperties() {
        if (properties.stream().noneMatch(prop -> prop.enabled() != prop.def()))
            return;

        String header = "*----------------- Controlify Debug Properties -----------------*";
        Controlify.LOGGER.error(header);

        int maxWidth = properties.stream().mapToInt(prop -> prop.name().length()).max().orElse(0);
        for (var prop : properties) {
            String line = "| %s%s = %s".formatted(prop.name(), " ".repeat(maxWidth - prop.name().length()), prop.enabled());
            line += " ".repeat(header.length() - line.length() - 1) + "|";

            Controlify.LOGGER.error(line);
        }

        Controlify.LOGGER.error("*---------------------------------------------------------------*");
    }

    private static boolean boolProp(String name, boolean defProd, boolean defDev) {
        boolean def = FabricLoader.getInstance().isDevelopmentEnvironment() ? defDev : defProd;
        boolean enabled = Boolean.parseBoolean(System.getProperty(name, Boolean.toString(def)));
        properties.add(new DebugProperty(name, enabled, def));
        return enabled;
    }

    private record DebugProperty(String name, boolean enabled, boolean def) {
    }
}
