package dev.isxander.controlify.splitscreen.window.embedder;

/**
 * Interface for embedding a child window inside a parent window.
 * This is used to create a splitscreen effect by embedding the child window
 * into the parent window at a specified position and size.
 */
public interface WindowManager {
    static WindowManager get() {
        return switch (dev.isxander.controlify.utils.WindowManager.INSTANCE) {
            case UNKNOWN -> throw new IllegalStateException("Unknown platform, cannot embed window");
            case X11 -> X11WindowManager.INSTANCE;
            case WAYLAND -> throw new IllegalStateException("Wayland is unsupported for Controlify splitscreen");
            case WIN32 -> Win32WindowManager.INSTANCE;
            case COCOA -> MacOSWindowManager.INSTANCE;
        };
    }

    /**
     * Get the native window handle for a given GLFW window handle.
     * This is used to be able to share the handle with other processes without
     * reading into another program's memory (which is not allowed).
     * @param glfwHandle glfw window handle
     * @return native window handle (e.g. HWND on Windows, XID on X11, NSWindow* on macOS)
     */
    NativeWindowHandle getNativeWindowHandle(long glfwHandle);

    /**
     * Embed a child window inside a parent window, positioning and sizing it.
     * @param childHandle  glfw window handle (e.g. HWND on Windows, XID on X11, NSWindow* on macOS)
     * @param parentHandle glfw parent window handle
     * @param x            x-offset within parent
     * @param y            y-offset within parent
     * @param width        width of the child
     * @param height       height of the child
     */
    void embedWindow(NativeWindowHandle childHandle, NativeWindowHandle parentHandle, int x, int y, int width, int height);
}
