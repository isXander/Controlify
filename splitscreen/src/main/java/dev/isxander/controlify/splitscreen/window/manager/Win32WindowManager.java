package dev.isxander.controlify.splitscreen.window.manager;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import net.minecraft.client.Minecraft;


import java.util.Objects;

import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinUser.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;

public class Win32WindowManager implements WindowManager {
    public static final Win32WindowManager INSTANCE = new Win32WindowManager();

    private static final User32 USER32 = User32.INSTANCE;

    @Override
    public NativeWindowHandle getNativeWindowHandle(long glfwHandle) {
        return new NativeWindowHandle(glfwGetWin32Window(glfwHandle));
    }

    @Override
    public void embedThisWindow(NativeWindowHandle parentHandle) {
        NativeWindowHandle childHandle = this.getNativeWindowHandle(
                Minecraft.getInstance().getWindow().getWindow()
        );

        HWND childHwnd = hwnd(childHandle);
        HWND parentHwnd = hwnd(parentHandle);

        USER32.SetParent(childHwnd, parentHwnd);

        int style = WS_CHILD | WS_VISIBLE;
        USER32.SetWindowLong(childHwnd, GWL_STYLE, style);
        USER32.SetWindowPos(childHwnd, null, 0, 0, 10, 10, SWP_NOZORDER | SWP_NOACTIVATE);
        USER32.SetForegroundWindow(parentHwnd);
    }

    @Override
    public boolean giveChildFocusIfParentIsForeground(NativeWindowHandle parentHandle, NativeWindowHandle childHandle) {
        HWND childHwnd = hwnd(childHandle);
        HWND parentHwnd = hwnd(parentHandle);

        HWND foregroundHwnd = USER32.GetForegroundWindow();
        if (Objects.equals(foregroundHwnd, parentHwnd)) {
            USER32.SetFocus(childHwnd);
            return true;
        }
        return false;
    }

    @Override
    public void setupWindowDims(NativeWindowHandle handle, int x, int y, int width, int height) {
        HWND windowHandle = hwnd(handle);

        USER32.SetWindowPos(windowHandle, null, x, y, width, height,
                SWP_NOZORDER | SWP_NOACTIVATE | SWP_SHOWWINDOW);
    }

    @Override
    public void hideWindow(NativeWindowHandle handle) {
        HWND windowHandle = hwnd(handle);

        USER32.ShowWindow(windowHandle, SW_HIDE);
    }

    @Override
    public void setWindowForeground(NativeWindowHandle handle) {
        HWND windowHandle = hwnd(handle);

        USER32.SetForegroundWindow(windowHandle);
    }

    @Override
    public void setWindowFocused(NativeWindowHandle handle) {
        HWND windowHandle = hwnd(handle);

        USER32.SetFocus(windowHandle);
    }

    private static HWND hwnd(NativeWindowHandle handle) {
        return new HWND(new Pointer(handle.handle()));
    }
}
