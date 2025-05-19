package dev.isxander.controlify.splitscreen.engine.impl.reparenting;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.engine.HostSplitscreenEngine;
import dev.isxander.controlify.splitscreen.engine.SplitscreenEnginePayloadSender;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ipc.ControllerboundThisIsMyWindowPayload;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.WindowManager;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.parent.ParentWindow;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.parent.ParentWindowEventHandler;
import dev.isxander.controlify.splitscreen.host.SplitscreenController;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class ReparentingHostSplitscreenEngine extends ReparentingSplitscreenEngine implements HostSplitscreenEngine, ParentWindowEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;

    private final ControllerUID localController;

    private final WindowManager windowManager;
    private @Nullable ParentWindow parentWindow;
    private final Queue<Consumer<@NotNull ParentWindow>> pendingWindowTasks = new ArrayDeque<>();
    private boolean dirty = false;

    private LocalReparentingPawn localPawn;
    private final Map<ControllerUID, ReparentingPawn> pawns = new HashMap<>();

    public ReparentingHostSplitscreenEngine(Minecraft minecraft, ControllerUID localController) {
        this.minecraft = minecraft;
        this.windowManager = WindowManager.get();
        this.localController = localController;
    }

    public static Optional<ReparentingHostSplitscreenEngine> tryGet(SplitscreenController controller) {
        if (controller.getSplitscreenEngine() instanceof ReparentingHostSplitscreenEngine reparenting) {
            return Optional.of(reparenting);
        }
        return Optional.empty();
    }

    public void initWindow(
            DisplayData screenSize,
            ScreenManager screenManager,
            String initialTitle
    ) {
        if (this.parentWindow != null) {
            LOGGER.warn("Parent window already initialized, skipping");
            return;
        }

        LOGGER.info("Setting up parent window");

        this.parentWindow = new ParentWindow(this.minecraft, screenSize, screenManager, this, initialTitle);
        this.localPawn = new LocalReparentingPawn(this.minecraft, this.windowManager.getNativeWindowHandle(this.minecraft.getWindow().getWindow()));
        this.registerPawn(this.localController, this.localPawn);

        this.parentWindow.setIcon(minecraft.getVanillaPackResources(), SharedConstants.getCurrentVersion().isStable() ? IconSet.RELEASE : IconSet.SNAPSHOT);

        while (!this.pendingWindowTasks.isEmpty()) {
            var task = this.pendingWindowTasks.poll();
            if (task != null) {
                task.accept(this.parentWindow);
            }
        }
    }

    public void registerPawn(ControllerUID window, ReparentingPawn pawn) {
        if (this.parentWindow == null) {
            throw new IllegalStateException("Parent window not yet initialised when remote pawn has already registered.");
        }

        this.pawns.put(window, pawn);
        this.windowManager.embedWindow(
                this.parentWindow.getNativeWindowHandle(),
                pawn.getNativeWindowHandle()
        );
        this.windowManager.setWindowForeground(this.parentWindow.getNativeWindowHandle());
        this.windowManager.setWindowFocused(pawn.getNativeWindowHandle());

        for (ReparentingPawn p : this.pawns.values()) {
            p.setWindowFocusState(true);
        }

        this.setDirty();
    }

    public void registerRemotePawn(ControllerUID window, Connection connection, ControllerboundThisIsMyWindowPayload payload) {
        if (this.parentWindow == null) {
            throw new IllegalStateException("Parent window not yet initialised when remote pawn has already registered.");
        }

        var sender = SplitscreenEnginePayloadSender.pawnbound(connection);
        RemoteReparentingPawn pawn = new RemoteReparentingPawn(sender, payload.windowHandle());
        this.registerPawn(window, pawn);
    }

    @Override
    public void setSplitscreenMode(ControllerUID window, SplitscreenPosition position) {
        ReparentingPawn pawn = this.getPawn(window);
        if (pawn == null) {
            LOGGER.warn("Tried to set splitscreen mode for a non-existent window {}", window);
            return;
        }

        NativeWindowHandle windowHandle = pawn.getNativeWindowHandle();

        switch (position) {
            case SplitscreenPosition.Visible visible -> {
                ScreenRectangle windowDims = visible.applyToRealDims(0, 0, this.parentWindow.getWidth(), this.parentWindow.getHeight());
                this.windowManager.setupWindowDims(windowHandle, windowDims.left(), windowDims.top(), windowDims.width(), windowDims.height());
            }
            case SplitscreenPosition.Hidden ignored -> {
                this.windowManager.hideWindow(windowHandle);
            }
        }
        pawn.setThrottleFramerate(position instanceof SplitscreenPosition.Hidden);
    }

    @Override
    public void removeWindow(ControllerUID window) {
        this.setDirty();
        this.pawns.remove(window);
    }

    public @Nullable ParentWindow getParentWindow() {
        return this.parentWindow;
    }

    @Override
    public boolean consumeDirty() {
        boolean wasDirty = this.dirty;
        this.dirty = false;
        return wasDirty;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    private void setDirty() {
        this.dirty = true;
    }

    @Override
    public boolean shouldExit() {
        if (this.parentWindow == null) {
            return false;
        }

        if (this.parentWindow.shouldClose()) {
            this.parentWindow.close();
            this.parentWindow = null;
            return true;
        }
        return false;
    }

    @Override
    public void onResizeParentWindow(int width, int height) {
        this.setDirty();
    }

    @Override
    public void onFocusParentWindow(boolean focused) {

    }

    @Override
    public void handleInboundPayload(ControllerUID window, Connection connection, CustomPacketPayload payload) {
        switch (payload) {
            case ControllerboundThisIsMyWindowPayload registerPayload ->
                this.registerRemotePawn(window, connection, registerPayload);
            default -> LOGGER.warn("Received unknown payload {}", payload.getClass().getSimpleName());
        }
    }

    private @Nullable ReparentingPawn getPawn(ControllerUID window) {
        return this.pawns.get(window);
    }


    private void executeWhenWindowReady(Consumer<@NotNull ParentWindow> task) {
        if (this.parentWindow != null) {
            task.accept(this.parentWindow);
        } else {
            this.pendingWindowTasks.add(task);
        }
    }
}
