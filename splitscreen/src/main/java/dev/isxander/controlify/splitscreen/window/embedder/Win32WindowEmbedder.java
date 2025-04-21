package dev.isxander.controlify.splitscreen.window.embedder;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;


import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinUser.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;

public class Win32WindowEmbedder implements WindowEmbedder {
    private static final User32 USER32 = User32.INSTANCE;

    @Override
    public void embedWindow(long childHandle, long parentHandle, int x, int y, int width, int height) {
        long nativeChildHandle = glfwGetWin32Window(childHandle);
        long nativeParentHandle = glfwGetWin32Window(parentHandle);

        HWND childHwnd = new HWND(new Pointer(nativeChildHandle));
        HWND parentHwnd = new HWND(new Pointer(nativeParentHandle));

        USER32.SetParent(childHwnd, parentHwnd);
        int style = WS_CHILD | WS_VISIBLE;
        USER32.SetWindowLong(childHwnd, GWL_STYLE, style);
        USER32.SetWindowPos(childHwnd, null, x, y, width, height, SWP_NOZORDER | SWP_NOACTIVATE);
    }
}
