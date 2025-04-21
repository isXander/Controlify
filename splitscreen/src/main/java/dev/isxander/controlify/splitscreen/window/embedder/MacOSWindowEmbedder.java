package dev.isxander.controlify.splitscreen.window.embedder;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow;

public class MacOSWindowEmbedder implements WindowEmbedder {
    private interface ObjCRuntime extends Library {
        ObjCRuntime INSTANCE = Native.load("objc", ObjCRuntime.class);
        Pointer sel_registerName(String name);
        Pointer objc_msgSend(Pointer receiver, Pointer selector, Object... args);
    }

    public static class NSPoint extends Structure {
        public double x, y;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("x","y"); }
    }
    public static class NSSize extends Structure {
        public double width, height;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("width","height"); }
    }
    public static class NSRect extends Structure {
        public NSPoint origin;
        public NSSize size;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("origin","size"); }
    }

    @Override
    public void embedWindow(long childHandle, long parentHandle, int x, int y, int width, int height) {
        long nativeChildHandle = glfwGetCocoaWindow(childHandle);
        long nativeParentHandle = glfwGetCocoaWindow(parentHandle);

        ObjCRuntime o = ObjCRuntime.INSTANCE;
        Pointer childWindow = new Pointer(nativeChildHandle);
        Pointer parentWindow = new Pointer(nativeParentHandle);

        Pointer selAddChild = o.sel_registerName("addChildWindow:ordered:");
        Pointer selSetFrame = o.sel_registerName("setFrame:display:");

        o.objc_msgSend(parentWindow, selAddChild, childWindow, 0);

        NSRect rect = new NSRect();
        rect.origin = new NSPoint(); rect.origin.x = x; rect.origin.y = y;
        rect.size = new NSSize(); rect.size.width = width; rect.size.height = height;
        o.objc_msgSend(childWindow, selSetFrame, rect, true);
    }
}
