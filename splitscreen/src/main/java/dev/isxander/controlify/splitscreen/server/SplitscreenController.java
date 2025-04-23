package dev.isxander.controlify.splitscreen.server;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SocketConnectionMethod;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.protocol.ControllerConnectionListener;
import dev.isxander.controlify.splitscreen.window.ParentWindow;
import dev.isxander.controlify.splitscreen.window.ParentWindowEventHandler;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_NO_ERROR;
import static org.lwjgl.glfw.GLFW.glfwGetError;

public class SplitscreenController implements ParentWindowEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final ControllerConnectionListener connectionListener;

    private @Nullable ParentWindow parentWindow;
    private final Queue<Runnable> waitingForWindowTasks = new ArrayDeque<>();

    public SplitscreenController(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        this.minecraft = minecraft;
        this.connectionListener = new ControllerConnectionListener(connectionMethod, this);
        this.addPawn(new LocalSplitscreenPawn(minecraft)); // control ourselves as a pawn
    }

    public void forEachPawn(Consumer<SplitscreenPawn> consumer) {
        pawns.forEach(consumer);
    }

    public void addPawn(SplitscreenPawn pawn) {
        int pawnIndex = this.pawns.size();
        LOGGER.info("Adding pawn #{}", pawnIndex);

        this.pawns.add(pawn);

        executeWhenWindowReady(parentWindow -> {
            pawn.setupWindowParent(
                    WindowManager.get().getNativeWindowHandle(parentWindow.getGlfwWindowHandle()),
                    1920 / 2 * pawnIndex, 0,
                    1920 / 2, 1080
            );
        });
    }

    public void removePawn(SplitscreenPawn pawn) {
        this.pawns.remove(pawn);
    }

    public @Nullable ParentWindow getParentWindow() {
        return this.parentWindow;
    }

    public void setupParentWindow(DisplayData screenSize, ScreenManager screenManager, String initialTitle) {
        RenderSystem.assertOnRenderThread();

        if (this.parentWindow != null) {
            LOGGER.warn("Parent window already set up, skipping");
            return;
        }

        this.parentWindow = new ParentWindow(this.minecraft, screenSize, screenManager, this, initialTitle);

        while (!this.waitingForWindowTasks.isEmpty()) {
            this.waitingForWindowTasks.poll().run();
        }
    }

    @Override
    public void onResizeParentWindow(int width, int height) {
        LOGGER.info("Resizing parent window to {}x{}", width, height);
        this.pawns.forEach(pawn -> pawn.setWindowSplitscreenMode(pawn.getWindowSplitscreenMode(), width, height));
    }

    public void negotiateSplitscreen() {

    }

    private void executeWhenWindowReady(Consumer<@NotNull ParentWindow> consumer) {
        if (this.parentWindow != null) {
            consumer.accept(this.parentWindow);
        } else {
            this.waitingForWindowTasks.add(() -> consumer.accept(this.parentWindow));
        }
    }
}
