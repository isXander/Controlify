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
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class SplitscreenController implements ParentWindowEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final ControllerConnectionListener connectionListener;
    private final LocalSplitscreenPawn localPawn;

    private final LocalControllerBridge controllerBridge;

    private @Nullable ParentWindow parentWindow;
    private boolean isWindowReady = false;
    private final Queue<Runnable> waitingForWindowTasks = new ArrayDeque<>();

    public SplitscreenController(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        this.minecraft = minecraft;
        this.controllerBridge = new LocalControllerBridge(minecraft, this);
        this.connectionListener = new ControllerConnectionListener(connectionMethod, this, minecraft);
        this.addPawn(this.localPawn = new LocalSplitscreenPawn(minecraft)); // control ourselves as a pawn
    }

    public void forEachPawn(Consumer<SplitscreenPawn> consumer) {
        pawns.forEach(consumer);
    }

    public void addPawn(SplitscreenPawn pawn) {
        int pawnIndex = this.pawns.size();
        LOGGER.info("Adding pawn #{}", pawnIndex);

        this.pawns.add(pawn);

        executeWhenWindowReady(parentWindow -> {
            SplitscreenPosition pos = pawnIndex == 0 ?
                    SplitscreenPosition.LEFT : SplitscreenPosition.RIGHT;
            ScreenRectangle windowDims = pos.applyToRealDims(0, 0, parentWindow.getWidth(), parentWindow.getHeight());

            // TODO: this basically repeats the below command, potentially remove window positioning from embedding code?
            pawn.setupWindowParent(
                    WindowManager.get().getNativeWindowHandle(parentWindow.getGlfwWindowHandle()),
                    windowDims.left(), windowDims.top(),
                    windowDims.width(), windowDims.height()
            );

            pawn.setWindowSplitscreenMode(pos, parentWindow.getWidth(), parentWindow.getHeight());
        });
    }

    public void removePawn(SplitscreenPawn pawn) {
        this.pawns.remove(pawn);
    }

    public LocalSplitscreenPawn getLocalPawn() {
        return this.localPawn;
    }

    public LocalControllerBridge getControllerBridge() {
        return this.controllerBridge;
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

        executeWhenWindowReady(parentWindow -> {
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                if (this.parentWindow.shouldClose() && this.minecraft.isRunning()) {
                    this.forEachPawn(SplitscreenPawn::closeGame);
                }
            });
        });

    }

    @Override
    public void onResizeParentWindow(int width, int height) {
        this.forEachPawn(pawn -> pawn.setWindowSplitscreenMode(pawn.getWindowSplitscreenMode(), width, height));
    }

    @Override
    public void onFocusParentWindow(boolean focused) {

    }

    public void negotiateSplitscreen() {

    }

    public void markWindowReady() {
        if (this.isWindowReady) return;
        if (this.parentWindow == null) throw new IllegalStateException("markWindowReady called before the ParentWindow has been created.");
        // noinspection ConstantConditions
        if (this.minecraft.getWindow() == null) throw new IllegalStateException("markWindowReady called before the vanilla Window has been created.");
        this.isWindowReady = true;

        while (!this.waitingForWindowTasks.isEmpty()) {
            this.waitingForWindowTasks.poll().run();
        }
    }

    private void executeWhenWindowReady(Consumer<@NotNull ParentWindow> consumer) {
        if (this.parentWindow != null) {
            consumer.accept(this.parentWindow);
        } else {
            this.waitingForWindowTasks.add(() -> consumer.accept(this.parentWindow));
        }
    }
}
