package dev.isxander.controlify.debug;

import net.fabricmc.loader.api.FabricLoader;

public class DebugProperties {
    /* Renders debug overlay for vmouse snapping */
    public static final boolean DEBUG_SNAPPING = boolProp("controlify.debug.snapping", false, false);
    /* Forces all gamepads to be treated as a regular joystick */
    public static final boolean FORCE_JOYSTICK = boolProp("controlify.debug.force_joystick", false, false);
    /* Prints joystick input counts for making joystick mappings */
    public static final boolean PRINT_JOY_INPUT_COUNT = boolProp("controlify.debug.print_joy_input_count", false, true);
    /* Print gyro data if supported */
    public static final boolean PRINT_GYRO = boolProp("controlify.debug.print_gyro", false, false);

    private static boolean boolProp(String name, boolean defProd, boolean defDev) {
        boolean def = FabricLoader.getInstance().isDevelopmentEnvironment() ? defDev : defProd;
        return Boolean.parseBoolean(System.getProperty(name, Boolean.toString(def)));
    }
}
