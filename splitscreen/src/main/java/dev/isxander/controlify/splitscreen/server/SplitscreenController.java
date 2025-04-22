package dev.isxander.controlify.splitscreen.server;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SocketConnectionMethod;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.client.ClientSplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.protocol.ControllerConnectionListener;
import dev.isxander.controlify.splitscreen.window.embedder.WindowManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_NO_ERROR;
import static org.lwjgl.glfw.GLFW.glfwGetError;

public class SplitscreenController {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final ControllerConnectionListener connectionListener;

    private long parentWindowHandle = 0;

    public SplitscreenController(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        this.connectionListener = new ControllerConnectionListener(connectionMethod, this);
        this.pawns.add(new ClientSplitscreenPawn(minecraft)); // control ourselves as a pawn
    }

    public void forEachPawn(Consumer<SplitscreenPawn> consumer) {
        pawns.forEach(consumer);
    }

    public void addPawn(SplitscreenPawn pawn) {
        LOGGER.info("Adding pawn #{}", this.pawns.size());

        this.pawns.add(pawn);
    }

    public void removePawn(SplitscreenPawn pawn) {
        this.pawns.remove(pawn);
    }

    public void setupParentWindow() {
        if (this.parentWindowHandle != 0) {
            LOGGER.warn("Parent window already set up, skipping");
            return;
        }

        long windowHandle = GLFW.glfwCreateWindow(1920, 1080, "Controlify Splitscreen Parent", 0, 0);
        if (windowHandle == 0) {
            handleLastGLFWError((error, description) -> {
                LOGGER.error("Failed to create GLFW window: {} ({})", description, error);
            });
            throw new RuntimeException("Failed to create GLFW window");
        }
        this.parentWindowHandle = windowHandle;

        WindowManager windowManager = WindowManager.get();
        WindowManager.get().embedWindow(
                windowManager.getNativeWindowHandle(Minecraft.getInstance().getWindow().getWindow()),
                windowManager.getNativeWindowHandle(windowHandle),
                0, 0,
                1920/2, 1080
        );
    }

    public long getParentWindowHandle() {
        return this.parentWindowHandle;
    }

    public void negotiateSplitscreen() {

    }

    private void handleLastGLFWError(BiConsumer<Integer, String> handler) {
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
