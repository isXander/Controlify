package dev.isxander.splitscreen.screenop;

/**
 * Setting to indicate how a screen should be displayed in splitscreen mode.
 */
public enum ScreenSplitscreenMode {
    /**
     * The screen should be displayed in fullscreen mode.
     * Remote clients will be hidden and only the host will
     * be able to control the screen.
     * <p>
     * This is intended for non-in-game screens,
     * like the title screen, or create world screen.
     */
    FULLSCREEN,
    /**
     * The screen should be displayed in splitscreen mode.
     * This means only an individual client needs to have it open,
     * and they don't need to be shared.
     * <p>
     * An example would be the pause screen, or container screens. (Opening chests)
     */
    SPLITSCREEN,
}
