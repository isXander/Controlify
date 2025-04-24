package dev.isxander.controlify.splitscreen.window.manager;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.minecraft.client.Minecraft;

import static org.lwjgl.glfw.GLFWNativeX11.glfwGetX11Window;

public class X11WindowManager implements WindowManager {
    public static final X11WindowManager INSTANCE = new X11WindowManager();

    private interface X11 extends Library {
        X11 INSTANCE = Native.load("X11", X11.class);

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

    public X11WindowManager() {
        display = X11.INSTANCE.XOpenDisplay(null);
        if (display == null) {
            throw new RuntimeException("Failed to open X11 display");
        }
    }

    @Override
    public NativeWindowHandle getNativeWindowHandle(long glfwHandle) {
        return new NativeWindowHandle(glfwGetX11Window(glfwHandle));
    }

    @Override
    public void embedThisWindow(NativeWindowHandle parentHandle, int x, int y, int width, int height) {
        NativeWindowHandle childHandle = this.getNativeWindowHandle(
                Minecraft.getInstance().getWindow().getWindow()
        );

        NativeLong childWindow = new NativeLong(childHandle.handle());
        NativeLong parentWindow = new NativeLong(parentHandle.handle());

        X11.INSTANCE.XReparentWindow(display, childWindow, parentWindow, x, y);
        X11.INSTANCE.XMoveResizeWindow(display, childWindow, x, y, width, height);
        X11.INSTANCE.XMapWindow(display, childWindow);
        X11.INSTANCE.XFlush(display);
    }

    @Override
    public boolean giveChildFocusIfParentIsForeground(NativeWindowHandle parentHandle, NativeWindowHandle childHandle) {
        // TODO
        return false;
    }
}
