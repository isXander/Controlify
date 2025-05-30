package dev.isxander.splitscreen.client.features.screenop;

/**
 * An interface implemented by {@link net.minecraft.client.gui.screens.Screen}s
 * to indicate how they should be handled in splitscreen mode.
 * @see ScreenSplitscreenModeRegistry
 */
public interface ScreenSplitscreenBehaviour {
    /**
     * This should return a constant, it should not change based on the state
     * of the screen, as it is only checked when it is set.
     */
    ScreenSplitscreenMode getSplitscreenMode();
}
