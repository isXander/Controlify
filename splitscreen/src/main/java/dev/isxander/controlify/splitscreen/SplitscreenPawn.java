package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;

public interface SplitscreenPawn {
    /**
     * Connects client to the given server.
     *
     * @param serverAddress address of the server
     * @param serverPort port of the server
     */
    void joinServer(String serverAddress, int serverPort);

    /**
     * Tells the pawn to move and configure the window to the given position.
     *
     * @param parentWindowHandle glfw window handle of the parent window
     */
    void configureWindow(long parentWindowHandle, SplitscreenPosition position);
}
