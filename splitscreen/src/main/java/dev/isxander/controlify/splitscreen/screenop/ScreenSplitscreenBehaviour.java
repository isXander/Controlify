package dev.isxander.controlify.splitscreen.screenop;

import net.minecraft.client.gui.screens.Screen;

/**
 * An interface implemented by {@link net.minecraft.client.gui.screens.Screen}s
 * to indicate how they should be handled in splitscreen mode.
 */
public interface ScreenSplitscreenBehaviour {
    static ScreenSplitscreenBehaviour fromScreen(Screen screen) {
        return (ScreenSplitscreenBehaviour) screen;
    }

    /**
     * This should return a constant, it should not change based on the state
     * of the screen, as it is only checked when it is set.
     */
    ScreenSplitscreenMode controlify$splitscreen$getMode();
}
