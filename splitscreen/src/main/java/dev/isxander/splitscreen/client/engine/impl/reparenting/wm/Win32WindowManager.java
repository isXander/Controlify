package dev.isxander.splitscreen.client.engine.impl.reparenting.wm;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;

import java.util.Objects;

import static com.sun.jna.platform.win32.WinDef.*;
import static org.lwjgl.glfw.GLFWNativeWin32.*;
import static org.lwjgl.system.windows.User32.*;

public class Win32WindowManager implements WindowManager {
    public static final Win32WindowManager INSTANCE = new Win32WindowManager();

    private static final User32 USER32 = User32.INSTANCE;

    @Override
    public NativeWindowHandle getNativeWindowHandle(long glfwHandle) {
        return new NativeWindowHandle(glfwGetWin32Window(glfwHandle));
    }

    @Override
    public void embedWindow(NativeWindowHandle parentHandle, NativeWindowHandle childHandle) {
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

    @Override
    public void setBorderless(NativeWindowHandle handle, boolean borderless, int x, int y, int width, int height) {
        HWND windowHandle = hwnd(handle);

        if (borderless) {
            // --- Modify Standard Styles (GWL_STYLE) ---
            int style = USER32.GetWindowLong(windowHandle, GWL_STYLE);

            // Styles to REMOVE for true borderless
            int stylesToRemove = WS_CAPTION |
                    WS_THICKFRAME |
                    WS_SYSMENU |
                    WS_MINIMIZEBOX |
                    WS_MAXIMIZEBOX |
                    WS_OVERLAPPED |  // Remove the overlapped type
                    WS_BORDER;      // Explicitly remove WS_BORDER

            // Styles to ADD for true borderless
            int stylesToAdd = WS_POPUP | WS_VISIBLE; // Use pure WS_POPUP

            style = (style & ~stylesToRemove) | stylesToAdd;
            USER32.SetWindowLong(windowHandle, GWL_STYLE, style);

            // --- Modify Extended Styles (GWL_EXSTYLE) ---
            int exStyle = USER32.GetWindowLong(windowHandle, GWL_EXSTYLE);

            // Extended styles to REMOVE for true borderless
            int exStylesToRemove = WS_EX_CLIENTEDGE |
                    WS_EX_WINDOWEDGE |
                    WS_EX_DLGMODALFRAME |
                    WS_EX_STATICEDGE;

            exStyle &= ~exStylesToRemove;
            USER32.SetWindowLong(windowHandle, GWL_EXSTYLE, exStyle);

            USER32.SetWindowPos(
                    windowHandle,
                    new HWND(Pointer.createConstant(HWND_TOP)),
                    x, y,
                    width, height,
                    SWP_FRAMECHANGED | SWP_SHOWWINDOW | SWP_NOACTIVATE
            );
        } else {
            // Restore to bordered (windowed) mode
            // This part should ideally restore original styles if you've saved them.
            // If not, set to a standard windowed configuration:

            int style = USER32.GetWindowLong(windowHandle, GWL_STYLE);
            style &= ~WS_POPUP; // Remove WS_POPUP if it was there
            style |= WS_OVERLAPPEDWINDOW | WS_VISIBLE; // Add standard overlapped window styles
            USER32.SetWindowLong(windowHandle, GWL_STYLE, style);

            int exStyle = USER32.GetWindowLong(windowHandle, GWL_EXSTYLE);
            // Add back typical extended styles for a windowed app, or restore original.
            // WS_EX_WINDOWEDGE was identified in your default GLFW window.
            exStyle |= WS_EX_WINDOWEDGE;
            // WS_EX_APPWINDOW should generally remain unless you explicitly removed it.
            // exStyle |= WinUser.WS_EX_APPWINDOW; // Usually already there from GLFW
            USER32.SetWindowLong(windowHandle, GWL_EXSTYLE, exStyle);

            // Your original flags for non-borderless SetWindowPos:
            // SWP_NOMOVE | SWP_NOSIZE means x, y, width, height are ignored here.
            // If you want to restore to a specific size/pos, remove those flags.
            int flags = SWP_FRAMECHANGED | SWP_SHOWWINDOW;
            // If you want to allow moving/sizing to the passed x,y,width,height when going windowed:
            // int flags = WinUser.SWP_FRAMECHANGED | WinUser.SWP_SHOWWINDOW;

            USER32.SetWindowPos(
                    windowHandle,
                    new HWND(Pointer.createConstant(HWND_NOTOPMOST)), // JNA provides HWND_NOTOPMOST
                    x, y, // These are ignored if SWP_NOMOVE | SWP_NOSIZE are used
                    width, height, // These are ignored if SWP_NOMOVE | SWP_NOSIZE are used
                    flags
            );
        }
    }

    private static HWND hwnd(NativeWindowHandle handle) {
        return new HWND(new Pointer(handle.handle()));
    }
}
