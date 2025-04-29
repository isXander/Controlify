package dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeX11;

import java.util.Objects;

import static com.sun.jna.platform.unix.X11.*;

public class X11WindowManager implements WindowManager {
    public static final X11WindowManager INSTANCE = new X11WindowManager();

    public interface XlibExt extends X11 {
        XlibExt INSTANCE = Native.load("X11", XlibExt.class);

        int XReparentWindow(Display display, NativeLong window, NativeLong newParent, int x, int y);

        int XSetInputFocus(Display display, NativeLong window, int revertTo, NativeLong time);
        int XGetInputFocus(Display display, WindowByReference focusWindow, IntByReference revertTo);
    }

    private static final XlibExt X = XlibExt.INSTANCE; // JNA Xlib extension instance

    private X11.Display display = null;

    private synchronized X11.Display getDisplay() {
        if (this.display == null) {
            long displayPtr = GLFWNativeX11.glfwGetX11Display();
            if (displayPtr == 0) {
                // Consider logging an error or throwing an exception
                System.err.println("Failed to get X11 display pointer from GLFW.");
                return null; // Or throw an exception
            }
            this.display = new X11.Display();
            this.display.setPointer(new Pointer(displayPtr));
        }
        return this.display;
    }

    @Override
    public NativeWindowHandle getNativeWindowHandle(long glfwHandle) {
        // On X11, the native handle is the Window XID (a long)
        long xid = GLFWNativeX11.glfwGetX11Window(glfwHandle);
        return new NativeWindowHandle(xid);
    }

    @Override
    public void embedWindow(NativeWindowHandle parentHandle, NativeWindowHandle childHandle) {
        X11.Display dpy = getDisplay();
        if (dpy == null) return;

        Window childXid = xid(childHandle);
        Window parentXid = xid(parentHandle);

        // Reparent the window. The child window becomes a subwindow of the parent.
        // The coordinates (0, 0) are relative to the parent window's origin.
        X.XReparentWindow(dpy, childXid, parentXid, 0, 0);

        // Ensure the child window is mapped (visible) after reparenting
        X.XMapWindow(dpy, childXid);

        // Unlike Win32, we don't typically need to set a special "child" style.
        // Reparenting handles the relationship.

        // Move/Resize the child to a small initial state within the parent, similar to the Win32 code.
        // Note: Size might be immediately overridden by layout logic later.
        X.XMoveResizeWindow(dpy, childXid, 0, 0, 10, 10);

        // Bring the parent window to the top of the stack and attempt to give it focus.
        // This is the closest equivalent to SetForegroundWindow on the parent.
        X.XRaiseWindow(dpy, parentXid);
        // Setting focus might be better handled by the window manager based on user action,
        // but we can try to set it to the parent initially.
        // Use RevertToParent: If the window loses focus, it reverts to the parent.
        X.XSetInputFocus(dpy, parentXid, X11.RevertToParent, new NativeLong(CurrentTime));

        // Flush the command buffer to the X server
        X.XFlush(dpy);
    }

    @Override
    public boolean giveChildFocusIfParentIsForeground(NativeWindowHandle parentHandle, NativeWindowHandle childHandle) {
        X11.Display dpy = getDisplay();
        if (dpy == null) return false;

        Window childXid = xid(childHandle);
        Window parentXid = xid(parentHandle);

        // Check which window currently has input focus.
        WindowByReference focusWindowRef = new WindowByReference();
        IntByReference revertToRef = new IntByReference();
        X.XGetInputFocus(dpy, focusWindowRef, revertToRef);

        Window currentFocusXid = focusWindowRef.getValue();

        // Check if the current focus window is the parent window.
        // Note: This is simpler than checking _NET_ACTIVE_WINDOW, but might not be
        // exactly the same as Win32's GetForegroundWindow if another window within
        // the parent application has focus.
        if (Objects.equals(currentFocusXid, parentXid)) {
            X.XSetInputFocus(dpy, childXid, X11.RevertToParent, new NativeLong(CurrentTime));
            X.XFlush(dpy);
            return true;
        }

        return false;
    }

    @Override
    public void setupWindowDims(NativeWindowHandle handle, int x, int y, int width, int height) {
        X11.Display dpy = getDisplay();
        if (dpy == null) return;

        Window windowXid = xid(handle);

        // Ensure width and height are at least 1, as X11 might reject 0.
        int w = Math.max(1, width);
        int h = Math.max(1, height);

        X.XMoveResizeWindow(dpy, windowXid, x, y, w, h);

        // Ensure the window is mapped (visible) when setting its dimensions.
        // It might have been hidden previously.
        X.XMapWindow(dpy, windowXid);

        X.XFlush(dpy);
    }

    @Override
    public void hideWindow(NativeWindowHandle handle) {
        X11.Display dpy = getDisplay();
        if (dpy == null) return;

        Window windowXid = xid(handle);

        // Unmap the window to make it invisible.
        X.XUnmapWindow(dpy, windowXid);
        X.XFlush(dpy);
    }

    @Override
    public void setWindowForeground(NativeWindowHandle handle) {
        X11.Display dpy = getDisplay();
        if (dpy == null) return;

        Window windowXid = xid(handle);

        // Raise the window to the top of the stacking order.
        X.XRaiseWindow(dpy, windowXid);

        // Set input focus to the window.
        // Use RevertToNone: If focus moves, it doesn't automatically revert to the parent.
        X.XSetInputFocus(dpy, windowXid, X11.RevertToNone, new NativeLong(X11.CurrentTime));

        // Optional: More robust activation using EWMH _NET_ACTIVE_WINDOW hint
        // This requires sending a ClientMessage event to the root window.
        // It's more complex and might be needed if XRaiseWindow + XSetInputFocus
        // isn't sufficient with some window managers.
        // sendNetActiveWindowMessage(dpy, windowXid); // Example helper function

        X.XFlush(dpy);
    }

    @Override
    public void setWindowFocused(NativeWindowHandle handle) {
        X11.Display dpy = getDisplay();
        if (dpy == null) return;

        Window windowXid = xid(handle);

        // Set input focus directly.
        // RevertToParent might be suitable if it's a child window.
        // RevertToNone might be better for a top-level window. Choose based on context.
        X.XSetInputFocus(dpy, windowXid, X11.RevertToParent, new NativeLong(X11.CurrentTime));
        X.XFlush(dpy);
    }

    // --- Helper Methods ---

    /**
     * Converts the NativeWindowHandle (containing the XID as a long)
     * to a JNA X11.Window object.
     */
    private static Window xid(NativeWindowHandle handle) {
        return new Window(handle.handle());
    }
}
