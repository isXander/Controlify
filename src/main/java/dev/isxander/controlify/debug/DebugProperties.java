package dev.isxander.controlify.debug;

public class DebugProperties {
    // Renders debug overlay for vmouse snapping
    public static final boolean DEBUG_SNAPPING = boolProp("controlify.debug.snapping", false);
    // Forces all gamepads to be treated as a regular joystick
    public static final boolean FORCE_JOYSTICK = boolProp("controlify.debug.force_joystick", false);

    private static boolean boolProp(String name, boolean def) {
        return Boolean.parseBoolean(System.getProperty(name, Boolean.toString(def)));
    }
}
