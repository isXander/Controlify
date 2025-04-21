package dev.isxander.controlify.splitscreen.window.embedder;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import static org.lwjgl.glfw.GLFWNativeX11.glfwGetX11Window;

public class X11WindowEmbedder implements WindowEmbedder {
    private interface X11Ext extends Library {
        X11Ext INSTANCE = Native.load("X11", X11Ext.class);

        // Display *XOpenDisplay(char *);
        Pointer XOpenDisplay(String display_name);
        // int XReparentWindow(Display *, Window, Window, int, int);
        int XReparentWindow(Pointer display, NativeLong window, NativeLong parent, int x, int y);
        // int XMoveResizeWindow(Display *, Window, int, int, unsigned int, unsigned int);
        int XMoveResizeWindow(Pointer display, NativeLong window, int x, int y, int width, int height);
        // int XMapWindow(Display *, Window);
        int XMapWindow(Pointer display, NativeLong window);
        // int XFlush(Display *);
        int XFlush(Pointer display);
    }

    private final Pointer display;

    public X11WindowEmbedder() {
        display = X11Ext.INSTANCE.XOpenDisplay(null);
        if (display == null) {
            throw new RuntimeException("Failed to open X11 display");
        }
    }

    @Override
    public void embedWindow(long childHandle, long parentHandle, int x, int y, int width, int height) {
        long nativeChildHandle = glfwGetX11Window(childHandle);
        long nativeParentHandle = glfwGetX11Window(parentHandle);

        NativeLong childWindow = new NativeLong(nativeChildHandle);
        NativeLong parentWindow = new NativeLong(nativeParentHandle);

        X11Ext.INSTANCE.XReparentWindow(display, childWindow, parentWindow, x, y);
        X11Ext.INSTANCE.XMoveResizeWindow(display, childWindow, x, y, width, height);
        X11Ext.INSTANCE.XMapWindow(display, childWindow);
        X11Ext.INSTANCE.XFlush(display);
    }
}
