package dev.isxander.controlify.debug;

import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;

import java.util.ArrayList;
import java.util.List;

public class DebugProperties {
    private static final List<DebugProperty> properties = new ArrayList<>();

    public static final boolean DEBUG_LOGGING = boolProp("controlify.debug.logging", false, true);
    /** Print the VID and PID of every controller connected. */
    public static final boolean PRINT_VID_PID = boolProp("controlify.debug.print_vid_pid", false, true);
    /** Renders debug overlay for vmouse snapping */
    public static final boolean DEBUG_SNAPPING = boolProp("controlify.debug.snapping", false, false);
    /** Forces all gamepads to be treated as a regular joystick */
    public static final boolean FORCE_JOYSTICK = boolProp("controlify.debug.force_joystick", false, false);
    /** Print what drivers are being used */
    public static final boolean PRINT_DRIVER = boolProp("controlify.debug.print_driver", true, true);
    /** Debug dumps after finishing init */
    public static final boolean INIT_DUMP = boolProp("controlify.debug.init_dump", false, true);

    public static void printProperties() {
        if (properties.stream().noneMatch(prop -> prop.enabled() != prop.def()))
            return;

        String header = "*----------------- Controlify Debug Properties -----------------*";
        CUtil.LOGGER.error(header);

        int maxWidth = properties.stream().mapToInt(prop -> prop.name().length()).max().orElse(0);
        for (var prop : properties) {
            String line = "| %s%s = %s".formatted(prop.name(), " ".repeat(maxWidth - prop.name().length()), prop.enabled());
            line += " ".repeat(header.length() - line.length() - 1) + "|";

            CUtil.LOGGER.error(line);
        }

        CUtil.LOGGER.error("*---------------------------------------------------------------*");
    }

    private static boolean boolProp(String name, boolean defProd, boolean defDev) {
        boolean def = PlatformMainUtil.isDevEnv() ? defDev : defProd;
        boolean enabled = Boolean.parseBoolean(System.getProperty(name, Boolean.toString(def)));
        properties.add(new DebugProperty(name, enabled, def));
        return enabled;
    }

    private record DebugProperty(String name, boolean enabled, boolean def) {
    }
}
