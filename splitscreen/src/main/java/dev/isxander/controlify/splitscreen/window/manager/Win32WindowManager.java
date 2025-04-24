package dev.isxander.controlify.splitscreen.window.manager;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import net.minecraft.client.Minecraft;


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
    public void embedThisWindow(NativeWindowHandle parentHandle, int x, int y, int width, int height) {
        NativeWindowHandle childHandle = this.getNativeWindowHandle(
                Minecraft.getInstance().getWindow().getWindow()
        );

        HWND childHwnd = new HWND(new Pointer(childHandle.handle()));
        HWND parentHwnd = new HWND(new Pointer(parentHandle.handle()));

        USER32.SetParent(childHwnd, parentHwnd);

        int style = WS_CHILD | WS_VISIBLE;
        USER32.SetWindowLong(childHwnd, GWL_STYLE, style);
        USER32.SetWindowPos(childHwnd, null, x, y, width, height, SWP_NOZORDER | SWP_NOACTIVATE);
    }

    @Override
    public boolean giveChildFocusIfParentIsForeground(NativeWindowHandle parentHandle, NativeWindowHandle childHandle) {
        HWND childHwnd = new HWND(new Pointer(childHandle.handle()));
        HWND parentHwnd = new HWND(new Pointer(parentHandle.handle()));

        HWND foregroundHwnd = USER32.GetForegroundWindow();
        System.out.println("Parent window:     " + parentHwnd);
        System.out.println("Foreground window: " + foregroundHwnd);
        System.out.println("Child window:      " + childHwnd);
        if (foregroundHwnd.equals(parentHwnd)) {
            System.out.println("Parent is foreground, setting focus to child");
            USER32.SetFocus(childHwnd);
            return true;
        }
        return false;
    }
}
