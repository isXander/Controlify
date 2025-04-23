package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import net.minecraft.client.Minecraft;

/**
 * Interface for a splitscreen pawn.
 * <p>
 * <ul>
 *     <li>If ran on the server, the pawn instance can control itself, or send packets to control remote pawns.</li>
 *     <li>If ran on the client, the packet listener will call upon a local pawn.</li>
 * </ul>
 * <p>
 * <strong>Unless otherwise stated, methods should expect to be run on the main thread.</strong>
 * 
 */
public interface SplitscreenPawn {
    /**
     * Connects client to the given server.
     *
     * @param serverAddress address of the server
     * @param serverPort port of the server
     */
    void joinServer(String serverAddress, int serverPort);

    /**
     * Sets this pawn's window to be a child of the given window.
     *
     * @param parentWindow the handle of the parent window
     * @param x relative x position of the child window
     * @param y relative y position of the child window
     * @param width width of the child window
     * @param height height of the child window
     */
    void setupWindowParent(NativeWindowHandle parentWindow, int x, int y, int width, int height);

    /**
     * Sets this pawn's window's splitscreen mode.
     *
     * @param position the splitscreen position of the window
     * @param parentWidth the width of the parent window
     * @param parentHeight the height of the parent window
     */
    void setWindowSplitscreenMode(SplitscreenPosition position, int parentWidth, int parentHeight);

    /**
     * Sets the client window's focus state.
     * {@link Minecraft#isWindowActive()}
     */
    void setWindowFocusState(boolean focused);

    /**
     * Closes the game.
     */
    void closeGame();

    /**
     * @return the current splitscreen mode of the window
     */
    SplitscreenPosition getWindowSplitscreenMode();
}
