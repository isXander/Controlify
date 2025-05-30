package dev.isxander.splitscreen.engine.impl.reparenting.manager;

/**
 * Interface for embedding a child window inside a parent window.
 * This is used to create a splitscreen effect by embedding the child window
 * into the parent window at a specified position and size.
 */
public interface WindowManager {
    static WindowManager get() {
        return switch (dev.isxander.controlify.utils.WindowManager.INSTANCE) {
            case WIN32 -> Win32WindowManager.INSTANCE;
            // Broken.
            case X11 -> throw new UnsupportedWindowManagerException("X11 is not supported");
            // Wayland does not support Reparenting windows, so it is impossible to do this.
            // XWayland would not crash, but Wayland has no way to represent this concept and XReparentWindow is no-op
            case WAYLAND -> throw new UnsupportedWindowManagerException("Wayland is unsupported for Controlify splitscreen");
            // Although Cocoa does support reparenting, it is impossible for two windows from different processes
            // to be reparented to each other. NSWindow#setParent(NSWindow) cannot work because either the parent
            // or child NSWindow pointer will be from a different process, violating the memory safety rules of macOS.
            case COCOA -> throw new UnsupportedWindowManagerException("macOS is unsupported for Controlify splitscreen");
            default -> throw new UnsupportedWindowManagerException("Unknown platform, cannot embed window");
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
     *
     * @param parentHandle underlying system's native window handle of the parent window
     * @param childHandle
     */
    void embedWindow(NativeWindowHandle parentHandle, NativeWindowHandle childHandle);

    /**
     * Gives the {@code childHandle} main focus if the {@code parentHandle} is in the foreground.
     * @param parentHandle underlying system's native window handle of the parent window
     * @param childHandle underlying system's native window handle of the child window
     * @return true if the child window was given focus, false otherwise
     */
    boolean giveChildFocusIfParentIsForeground(NativeWindowHandle parentHandle, NativeWindowHandle childHandle);

    /**
     * Configure the dimensions of the window, also makes it visible.
     *
     * @param handle native window handle
     */
    void setupWindowDims(NativeWindowHandle handle, int x, int y, int width, int height);

    /**
     * Hide the window.
     *
     * @param handle native window handle
     */
    void hideWindow(NativeWindowHandle handle);

    void setWindowForeground(NativeWindowHandle handle);

    void setWindowFocused(NativeWindowHandle handle);

    void setBorderless(NativeWindowHandle handle, boolean borderless, int x, int y, int width, int height);
}
