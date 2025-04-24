package dev.isxander.controlify.splitscreen;

/**
 * Facilitates communication between a client and the controller.
 * <ul>
 *     <li>On the host side, this is a thin bridge that calls directly.</li>
 *     <li>On the remote side, this sends packets to the controller.</li>
 * </ul>
 */
public interface ControllerBridge {
    /**
     * Sets the caller's client window to be in focus, if the parent window is in foreground.
     */
    void giveFocusToMeIfForeground();
}
