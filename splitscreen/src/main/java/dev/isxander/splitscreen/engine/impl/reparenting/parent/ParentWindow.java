package dev.isxander.splitscreen.engine.impl.reparenting.parent;

import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import dev.isxander.splitscreen.engine.impl.reparenting.manager.WindowManager;
import dev.isxander.splitscreen.mixins.engine.reparent.ScreenManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Creates and manages the parent window that all pawns attach to.
 * This allows Controlify to emulate a native splitscreen experience by
 * making it look like a single window.
 * <p>
 * This window become the sole one visible to the user, meaning the icon, title, etc,
 * must be propagated to this window.
 * @implNote Window attributes are only propagated from the host window, not the remote pawns.
 */
public class ParentWindow implements AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ScreenManager screenManager;
    private final Minecraft minecraft;
    private final ParentWindowEventHandler eventHandler;

    private final long glfwWindowHandle;

    // current x y of the window
    private int x, y;
    // cached the position in window mode so it can be restored when exiting fullscreen
    private int windowedX, windowedY;

    // current width and height of the window
    private int width, height;
    // cache the dimensions in window mode so it can be restored when exiting fullscreen
    private int windowedWidth, windowedHeight;

    // current desired fullscreen state
    private boolean fullscreen;
    // if the window state is currently fullscreen
    private boolean actualFullscreen;

    public ParentWindow(Minecraft minecraft, DisplayData screenSize, ScreenManager screenManager, ParentWindowEventHandler eventHandler, String initialTitle) {
        this.screenManager = screenManager;
        this.minecraft = minecraft;
        this.eventHandler = eventHandler;

        Monitor monitor = screenManager.getMonitor(glfwGetPrimaryMonitor());
        this.width = this.windowedWidth = Math.max(screenSize.width(), 1);
        this.height = this.windowedHeight = Math.max(screenSize.height(), 1);
        this.fullscreen = screenSize.isFullscreen();
        this.actualFullscreen = false;

        glfwDefaultWindowHints();
        this.glfwWindowHandle = glfwCreateWindow(
                this.width, this.height,
                initialTitle,
                0L, // only needs setting if fullscreen it seems (following vanilla behaviour)
                0L
        );
        if (this.glfwWindowHandle == 0L) {
            handleLastGLFWError((errorCode, description) -> {
                throw new RuntimeException("Failed to create GLFW window: " + description, new Throwable());
            });
        }

        glfwSetWindowFocusCallback(this.glfwWindowHandle, this::windowFocusCallback);
        glfwSetWindowSizeCallback(this.glfwWindowHandle, this::windowPosCallback);
        glfwSetWindowSizeCallback(this.glfwWindowHandle, this::windowSizeCallback);

        int[] xBuffer = new int[1];
        int[] yBuffer = new int[1];
        glfwGetWindowPos(this.glfwWindowHandle, xBuffer, yBuffer);
        this.x = this.windowedX = xBuffer[0];
        this.y = this.windowedY = yBuffer[0];

        this.updateFullscreen();
    }

    public long getGlfwWindowHandle() {
        return this.glfwWindowHandle;
    }

    public NativeWindowHandle getNativeWindowHandle() {
        return WindowManager.get().getNativeWindowHandle(this.getGlfwWindowHandle());
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public void setWindowed(int width, int height) {
        this.width = width;
        this.height = height;
        this.fullscreen = false;
        this.updateFullscreen();
        this.flushWindowDimensions();
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        this.updateFullscreen();
    }

    public void toggleFullscreen() {
        this.fullscreen = !this.fullscreen;
        this.updateFullscreen();
    }

    private void updateFullscreen() {
        if (this.fullscreen != this.actualFullscreen) {
            var windowManager = WindowManager.get();

            if (this.fullscreen) {
                Monitor bestMonitor = this.findBestMonitor();
                if (bestMonitor == null) {
                    LOGGER.error("Failed to fullscreen update: no best monitor found");
                    this.fullscreen = false;
                    return;
                }

                VideoMode videoMode = bestMonitor.getCurrentMode();
                this.windowedX = this.x;
                this.windowedY = this.y;
                this.windowedWidth = this.width;
                this.windowedHeight = this.height;

                this.x = 0;
                this.y = 0;
                this.width = videoMode.getWidth();
                this.height = videoMode.getHeight();

                this.actualFullscreen = true;

                windowManager.setBorderless(
                        windowManager.getNativeWindowHandle(this.glfwWindowHandle),
                        true,
                        this.x, this.y,
                        this.width, this.height
                );
            } else {
                this.x = this.windowedX;
                this.y = this.windowedY;
                this.width = this.windowedWidth;
                this.height = this.windowedHeight;

                this.actualFullscreen = false;

                windowManager.setBorderless(
                        windowManager.getNativeWindowHandle(this.glfwWindowHandle),
                        false,
                        this.x, this.y,
                        this.width, this.height
                );
            }
        }
    }

    private void flushWindowDimensions() {
        glfwSetWindowPos(this.glfwWindowHandle, this.x, this.y);
        glfwSetWindowSize(this.glfwWindowHandle, this.width, this.height);
    }

    public void setTitle(String title) {
        RenderSystem.assertOnRenderThread();
        glfwSetWindowTitle(this.glfwWindowHandle, title + " - Controlify Splitscreen");
    }

    public void setIcon(PackResources resources, IconSet iconSet) {
        try {
            switch (glfwGetPlatform()) {
                case GLFW_PLATFORM_WIN32, GLFW_PLATFORM_X11 -> {
                    List<IoSupplier<InputStream>> icons = iconSet.getStandardIcons(resources);
                    List<ByteBuffer> iconBuffers = new ArrayList<>(icons.size());

                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        GLFWImage.Buffer imgBuffer = GLFWImage.malloc(icons.size(), stack);

                        for (int i = 0; i < icons.size(); i++) {
                            try (NativeImage image = NativeImage.read(icons.get(i).get())) {
                                ByteBuffer iconBuffer = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * NativeImage.Format.RGBA.components());
                                iconBuffers.add(iconBuffer);
                                iconBuffer.asIntBuffer().put(image.getPixelsABGR());

                                imgBuffer.position(i);
                                imgBuffer.width(image.getWidth());
                                imgBuffer.height(image.getHeight());
                                imgBuffer.pixels(iconBuffer);
                            }
                        }
                        imgBuffer.flip();

                        this.setIcon(imgBuffer);
                    } finally {
                        iconBuffers.forEach(MemoryUtil::memFree);
                    }

                }
                // macOS sets icon application-wide, this is done by vanilla Window so we don't need to propagate here
                case GLFW_PLATFORM_COCOA -> { /* no-op */ }
                // Wayland doesn't support changing icons
                case GLFW_PLATFORM_WAYLAND -> { /* no-op */ }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to set window icon", e);
        }
    }

    public void setIcon(GLFWImage.Buffer iconBuffer) {
        RenderSystem.assertOnRenderThread();

        iconBuffer.mark();
        iconBuffer.position(0);

        glfwSetWindowIcon(this.glfwWindowHandle, iconBuffer);

        iconBuffer.reset();
    }

    public boolean shouldClose() {
        RenderSystem.assertOnRenderThread();
        return glfwWindowShouldClose(this.glfwWindowHandle);
    }

    private void windowFocusCallback(long window, boolean focused) {
        if (window != this.glfwWindowHandle) return;

        this.eventHandler.onFocusParentWindow(focused);
    }

    private void windowPosCallback(long window, int x, int y) {
        if (window != this.glfwWindowHandle) return;

        this.x = x;
        this.y = y;
    }

    private void windowSizeCallback(long window, int width, int height) {
        if (window != this.glfwWindowHandle) return;

        this.width = width;
        this.height = height;

        this.eventHandler.onResizeParentWindow(width, height);
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        glfwFreeCallbacks(this.glfwWindowHandle);
        glfwDestroyWindow(this.glfwWindowHandle);
    }

    private @Nullable Monitor findBestMonitor() {
        int windowLeftX = this.x;
        int windowRightX = windowLeftX + this.width;
        int windowTopY = this.y;
        int windowBottomY = windowTopY + this.height;

        int maxOverlapArea = -1;
        Monitor bestMonitor = null;

        long primaryMonitorHandle = glfwGetPrimaryMonitor();

        for (Monitor monitor : ((ScreenManagerAccessor) this.screenManager).getMonitors().values()) {
            int monitorLeftX = monitor.getX();
            int monitorRightX = monitorLeftX + monitor.getCurrentMode().getWidth();
            int monitorTopY = monitor.getY();
            int monitorBottomY = monitorTopY + monitor.getCurrentMode().getHeight();

            int clampedWindowLeftX = Mth.clamp(windowLeftX, monitorLeftX, monitorRightX);
            int clampedWindowRightX = Mth.clamp(windowRightX, monitorLeftX, monitorRightX);
            int clampedWindowTopY = Mth.clamp(windowTopY, monitorTopY, monitorBottomY);
            int clampedWindowBottomY = Mth.clamp(windowBottomY, monitorTopY, monitorBottomY);

            int overlapWidth = Math.max(0, clampedWindowRightX - clampedWindowLeftX);
            int overlapHeight = Math.max(0, clampedWindowBottomY - clampedWindowTopY);
            int overlapArea = overlapWidth * overlapHeight;

            if (overlapArea > maxOverlapArea) {
                maxOverlapArea = overlapArea;
                bestMonitor = monitor;
            } else if (overlapArea == maxOverlapArea && primaryMonitorHandle == monitor.getMonitor()) {
                bestMonitor = monitor;
            }
        }

        return bestMonitor;
    }

    private static void handleLastGLFWError(BiConsumer<Integer, String> handler) {
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            int error = glfwGetError(pointerbuffer);
            if (error != GLFW_NO_ERROR) {
                long pDescription = pointerbuffer.get();
                String description = pDescription == 0L ? "" : MemoryUtil.memUTF8(pDescription);
                handler.accept(error, description);
            }
        }
    }
}
