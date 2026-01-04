package dev.isxander.controlify.api.v1;

/**
 * Represents a specific game controller.
 * This could be a gamepad, or a generic joystick device.
 * <p>
 * Each controller has different capabilities and button layouts.
 */
public interface ControlifyController {

    /**
     * Gets the unique identifier for this controller.
     * This identifier is stable and will be the same across sessions for the same
     * physical controller. If there are multiple controllers of the same type connected,
     * they will have different UIDs, the first to connect will have the same UID across sessions.
     * @return the unique identifier for this controller.
     */
    String uid();

}
