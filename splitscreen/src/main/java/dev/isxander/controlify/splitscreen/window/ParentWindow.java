package dev.isxander.controlify.splitscreen.window;

import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
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
    private int width, height;

    public ParentWindow(Minecraft minecraft, DisplayData screenSize, ScreenManager screenManager, ParentWindowEventHandler eventHandler, String initialTitle) {
        this.screenManager = screenManager;
        this.minecraft = minecraft;
        this.eventHandler = eventHandler;

        Monitor monitor = screenManager.getMonitor(glfwGetPrimaryMonitor());
        this.width = Math.max(screenSize.width(), 1);
        this.height = Math.max(screenSize.height(), 1);

        glfwDefaultWindowHints();
        this.glfwWindowHandle = glfwCreateWindow(
                this.width, this.height,
                initialTitle,
                0L, // only needs setting if splitscreen it seems (following vanilla behaviour)
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
    }

    public long getGlfwWindowHandle() {
        return this.glfwWindowHandle;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setTitle(String title) {
        RenderSystem.assertOnRenderThread();
        glfwSetWindowTitle(this.glfwWindowHandle, title + " - Controlify Splitscreen");
    }

    public void setIcon(PackResources resources, IconSet iconSet) throws IOException {
        switch (glfwGetPlatform()) {
            case GLFW_PLATFORM_WIN32, GLFW_PLATFORM_X11 -> {
                List<IoSupplier<InputStream>> icons = iconSet.getStandardIcons(resources);
                List<ByteBuffer> iconBuffers = new ArrayList<>(icons.size());

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    try (GLFWImage.Buffer imgBuffer = GLFWImage.malloc(icons.size(), stack)) {
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
                    }
                } finally {
                    iconBuffers.forEach(MemoryUtil::memFree);
                }

            }
            // macOS sets icon application-wide, this is done by vanilla Window so we don't need to propagate here
            case GLFW_PLATFORM_COCOA -> { /* no-op */ }
            // Wayland doesn't support changing icons
            case GLFW_PLATFORM_WAYLAND -> { /* no-op */ }
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

        // TODO: determine if the parent window should terminate or if the client's window should terminate
        //glfwTerminate();
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
