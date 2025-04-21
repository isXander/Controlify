package dev.isxander.controlify.splitscreen.window.embedder;

import dev.isxander.controlify.utils.WindowManager;

/**
 * Interface for embedding a child window inside a parent window.
 * This is used to create a splitscreen effect by embedding the child window
 * into the parent window at a specified position and size.
 */
public interface WindowEmbedder {
    static WindowEmbedder get() {
        return switch (WindowManager.INSTANCE) {
            case UNKNOWN -> throw new IllegalStateException("Unknown platform, cannot embed window");
            case X11, WAYLAND -> new X11WindowEmbedder(); // Wayland uses Xwayland - native wayland embedding is not supported
            case WIN32 -> new Win32WindowEmbedder();
            case COCOA -> new MacOSWindowEmbedder();
        };
    }

    /**
     * Embed a child window inside a parent window, positioning and sizing it.
     * @param childHandle  glfw window handle (e.g. HWND on Windows, XID on X11, NSWindow* on macOS)
     * @param parentHandle glfw parent window handle
     * @param x            x-offset within parent
     * @param y            y-offset within parent
     * @param width        width of the child
     * @param height       height of the child
     */
    void embedWindow(long childHandle, long parentHandle, int x, int y, int width, int height);
}
